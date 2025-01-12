package org.koitharu.kotatsu.parsers.site.mangabox

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Document
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.PagedMangaParser
import org.koitharu.kotatsu.parsers.model.*
import org.koitharu.kotatsu.parsers.util.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal abstract class MangaboxParser(
	context: MangaLoaderContext,
	source: MangaSource,
	pageSize: Int = 24,
) : PagedMangaParser(context, source, pageSize) {

	override val sortOrders: Set<SortOrder> = EnumSet.of(
		SortOrder.UPDATED,
		SortOrder.POPULARITY,
		SortOrder.NEWEST,
	)

	protected open val listUrl = "/genre-all"
	protected open val searchUrl = "/search/story/"
	protected open val datePattern = "MMM dd,yy"


	init {
		paginator.firstPage = 1
		searchPaginator.firstPage = 1
	}


	@JvmField
	protected val ongoing: Set<String> = setOf(
		"Ongoing",
	)

	@JvmField
	protected val finished: Set<String> = setOf(
		"Completed",
	)

	override suspend fun getListPage(
		page: Int,
		query: String?,
		tags: Set<MangaTag>?,
		sortOrder: SortOrder,
	): List<Manga> {
		val tag = tags.oneOrThrowIfMany()
		val url = buildString {
			append("https://")
			append(domain)

			if (!query.isNullOrEmpty()) {
				append(searchUrl)
				append(query.urlEncoded())
				append("?page=")
				append(page.toString())

			} else if (!tags.isNullOrEmpty()) {
				append("/")
				append(tag?.key.orEmpty())
				append("/")
				append(page.toString())
			} else {
				append("$listUrl/")
				if (page > 1) {
					append(page.toString())
				}
				when (sortOrder) {
					SortOrder.POPULARITY -> append("?type=topview")
					SortOrder.UPDATED -> append("")
					SortOrder.NEWEST -> append("?type=newest")
					else -> append("")
				}
			}


		}

		val doc = webClient.httpGet(url).parseHtml()

		return doc.select("div.content-genres-item, div.list-story-item").ifEmpty {
			doc.select("div.search-story-item")
		}.map { div ->
			val href = div.selectFirstOrThrow("a").attrAsRelativeUrl("href")
			Manga(
				id = generateUid(href),
				url = href,
				publicUrl = href.toAbsoluteUrl(div.host ?: domain),
				coverUrl = div.selectFirst("img")?.src().orEmpty(),
				title = div.selectFirstOrThrow("h3").text().orEmpty(),
				altTitle = null,
				rating = RATING_UNKNOWN,
				tags = emptySet(),
				author = null,
				state = null,
				source = source,
				isNsfw = isNsfwSource,
			)
		}
	}

	protected open val selectTagMap = "div.panel-genres-list a:not(.genres-select)"

	override suspend fun getTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/$listUrl").parseHtml()
		return doc.select(selectTagMap).mapNotNullToSet { a ->
			val key = a.attr("href").removeSuffix('/').substringAfterLast('/')
			val name = a.attr("title").replace(" Manga", "")
			MangaTag(
				key = key,
				title = name,
				source = source,
			)
		}
	}

	protected open val selectDesc = "div#noidungm, div#panel-story-info-description"
	protected open val selectState = "li:contains(status), td:containsOwn(status) + td"
	protected open val selectAlt = ".story-alternative, tr:has(.info-alternative) h2"
	protected open val selectAut = "li:contains(author) a, td:contains(author) + td a"
	protected open val selectTag = "div.manga-info-top li:contains(genres) a , td:containsOwn(genres) + td a"

	override suspend fun getDetails(manga: Manga): Manga = coroutineScope {
		val fullUrl = manga.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()

		val chaptersDeferred = async { getChapters(manga, doc) }

		val desc = doc.selectFirstOrThrow(selectDesc).html()

		val stateDiv = doc.select(selectState).text()

		val state = stateDiv.let {
			when (it) {
				in ongoing -> MangaState.ONGOING
				in finished -> MangaState.FINISHED
				else -> null
			}
		}

		val alt = doc.body().select(selectAlt).text().replace("Alternative : ", "")

		val aut = doc.body().select(selectAut).eachText().joinToString()

		manga.copy(
			tags = doc.body().select(selectTag).mapNotNullToSet { a ->
				MangaTag(
					key = a.attr("href").substringAfterLast("category=").substringBefore("&"),
					title = a.text().toTitleCase(),
					source = source,
				)
			},
			description = desc,
			altTitle = alt,
			author = aut,
			state = state,
			chapters = chaptersDeferred.await(),
			isNsfw = manga.isNsfw,
		)
	}

	protected open val selectDate = "span"
	protected open val selectChapter = "div.chapter-list div.row, ul.row-content-chapter li"

	protected open suspend fun getChapters(manga: Manga, doc: Document): List<MangaChapter> {
		val dateFormat = SimpleDateFormat(datePattern, sourceLocale)
		return doc.body().select(selectChapter).mapChapters(reversed = true) { i, li ->
			val a = li.selectFirstOrThrow("a")
			val href = a.attrAsRelativeUrl("href")
			val dateText = li.select(selectDate).last()?.text()

			MangaChapter(
				id = generateUid(href),
				name = a.text(),
				number = i + 1,
				url = href,
				uploadDate = parseChapterDate(
					dateFormat,
					dateText,
				),
				source = source,
				scanlator = null,
				branch = null,
			)
		}
	}

	protected open val selectPage = "div#vungdoc img, div.container-chapter-reader img"

	protected open val otherDomain = ""

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()

		if (doc.select(selectPage).isNullOrEmpty()) {
			val fullUrl2 = chapter.url.toAbsoluteUrl(domain).replace(domain, otherDomain)
			val doc2 = webClient.httpGet(fullUrl2).parseHtml()

			return doc2.select(selectPage).map { img ->
				val url = img.src()?.toRelativeUrl(domain) ?: img.parseFailed("Image src not found")

				MangaPage(
					id = generateUid(url),
					url = url,
					preview = null,
					source = source,
				)
			}
		} else {
			return doc.select(selectPage).map { img ->
				val url = img.src()?.toRelativeUrl(domain) ?: img.parseFailed("Image src not found")

				MangaPage(
					id = generateUid(url),
					url = url,
					preview = null,
					source = source,
				)
			}
		}

	}

	protected fun parseChapterDate(dateFormat: DateFormat, date: String?): Long {
		// Clean date (e.g. 5th December 2019 to 5 December 2019) before parsing it
		val d = date?.lowercase() ?: return 0
		return when {
			d.endsWith(" ago") ||
				// short Hours
				d.endsWith(" h") ||
				// short Day
				d.endsWith(" d") -> parseRelativeDate(date)

			// Handle 'yesterday' and 'today', using midnight
			d.startsWith("year") -> Calendar.getInstance().apply {
				add(Calendar.DAY_OF_MONTH, -1) // yesterday
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.SECOND, 0)
				set(Calendar.MILLISECOND, 0)
			}.timeInMillis

			d.startsWith("today") -> Calendar.getInstance().apply {
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.SECOND, 0)
				set(Calendar.MILLISECOND, 0)
			}.timeInMillis

			date.contains(Regex("""\d(st|nd|rd|th)""")) -> date.split(" ").map {
				if (it.contains(Regex("""\d\D\D"""))) {
					it.replace(Regex("""\D"""), "")
				} else {
					it
				}
			}.let { dateFormat.tryParse(it.joinToString(" ")) }

			else -> dateFormat.tryParse(date)
		}
	}

	// Parses dates in this form:
	// 21 hours ago
	private fun parseRelativeDate(date: String): Long {
		val number = Regex("""(\d+)""").find(date)?.value?.toIntOrNull() ?: return 0
		val cal = Calendar.getInstance()

		return when {
			WordSet(
				"day",
				"days",
			).anyWordIn(date) -> cal.apply { add(Calendar.DAY_OF_MONTH, -number) }.timeInMillis

			WordSet("hour", "hours", "h").anyWordIn(date) -> cal.apply {
				add(
					Calendar.HOUR,
					-number,
				)
			}.timeInMillis

			WordSet(
				"min",
				"minute",
				"minutes",
			).anyWordIn(date) -> cal.apply {
				add(
					Calendar.MINUTE,
					-number,
				)
			}.timeInMillis

			WordSet("second").anyWordIn(date) -> cal.apply {
				add(
					Calendar.SECOND,
					-number,
				)
			}.timeInMillis

			WordSet("month", "months").anyWordIn(date) -> cal.apply { add(Calendar.MONTH, -number) }.timeInMillis
			WordSet("year").anyWordIn(date) -> cal.apply { add(Calendar.YEAR, -number) }.timeInMillis
			else -> 0
		}
	}

}
