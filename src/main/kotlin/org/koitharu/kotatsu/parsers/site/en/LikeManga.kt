package org.koitharu.kotatsu.parsers.site.en

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.nodes.Element
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.PagedMangaParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.*
import org.koitharu.kotatsu.parsers.util.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@MangaSourceParser("LIKEMANGA", "LikeManga", "en")
internal class LikeManga(context: MangaLoaderContext) : PagedMangaParser(context, MangaSource.LIKEMANGA, 36) {

	override val sortOrders: Set<SortOrder> = EnumSet.of(SortOrder.UPDATED, SortOrder.POPULARITY, SortOrder.NEWEST)
	override val configKeyDomain = ConfigKey.Domain("likemanga.io")

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
			append("/?act=search&f")
			append("[sortby]".urlEncoded())
			append("=")
			when (sortOrder) {
				SortOrder.POPULARITY -> append("hot")
				SortOrder.UPDATED -> append("lastest-chap")
				SortOrder.NEWEST -> append("lastest-manga")
				else -> append("lastest-chap")
			}
			if (page > 1) {
				append("&pageNum=")
				append(page)
			}
			if (!tags.isNullOrEmpty()) {
				append("&f")
				append("[genres]".urlEncoded())
				append("=")
				append(tag?.key.orEmpty())
			}
			if (!query.isNullOrEmpty()) {
				append("&f")
				append("[keyword]".urlEncoded())
				append("=")
				append(query.urlEncoded())
			}
		}
		val doc = webClient.httpGet(url).parseHtml()
		return doc.select("div.card-body div.video").map { div ->
			val href = div.selectFirstOrThrow("a").attrAsRelativeUrl("href")
			Manga(
				id = generateUid(href),
				title = div.selectFirstOrThrow("p.title-manga").text(),
				altTitle = null,
				url = href,
				publicUrl = href.toAbsoluteUrl(domain),
				rating = RATING_UNKNOWN,
				isNsfw = false,
				coverUrl = div.selectFirstOrThrow("img").src()?.toAbsoluteUrl(domain).orEmpty(),
				tags = emptySet(),
				state = null,
				author = null,
				source = source,
			)
		}
	}

	override suspend fun getTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/genres/").parseHtml()
		return doc.select("ul.nav-genres li:not(.text-center) a").mapNotNullToSet { a ->
			MangaTag(
				key = a.attr("href").removeSuffix("/").substringAfterLast('/'),
				title = a.text(),
				source = source,
			)
		}
	}

	override suspend fun getDetails(manga: Manga): Manga {
		val doc = webClient.httpGet(manga.url.toAbsoluteUrl(domain)).parseHtml()
		val mangaId = manga.url.toAbsoluteUrl(domain).removeSuffix("/").substringAfterLast("-").toInt()
		val maxPageChapterSelect = doc.getElementById("nav_list_chapter_id_detail")?.select("a:not(.next)")
		var maxPageChapter = 1
		if (!maxPageChapterSelect.isNullOrEmpty()) {
			maxPageChapterSelect.map {
				val i = it.text().toInt()
				if (i > maxPageChapter) {
					maxPageChapter = i
				}
			}
		}
		return manga.copy(
			altTitle = doc.selectFirstOrThrow(".list-info li.othername h2").text(),
			state = null,
			tags = doc.select("li.kind a").mapNotNullToSet { a ->
				MangaTag(
					key = a.attr("href").removeSuffix("/").substringAfterLast('/'),
					title = a.text().toTitleCase(),
					source = source,
				)
			},
			author = doc.select("li.author p").last()?.text(),
			description = doc.requireElementById("summary_shortened").text(),
			chapters = run {
				if (maxPageChapter == 1) {
					parseChapters(doc)
				} else {
					coroutineScope {
						val result = ArrayList(parseChapters(doc))
						result.ensureCapacity(result.size * maxPageChapter)
						(2..maxPageChapter).map { i ->
							async {
								loadChapters(mangaId, i)
							}
						}.awaitAll()
							.flattenTo(result)
						result
					}
				}
			}.reversed(),
		)
	}

	private suspend fun loadChapters(mangaId: Int, page: Int): List<MangaChapter> {
		val json =
			webClient.httpGet(
				"https://$domain/?act=ajax&code=load_list_chapter&manga_id=$mangaId&page_num=$page&chap_id=0&keyword=",
			)
				.parseJson().getString("list_chap")
		val chapters = json.split("wp-manga-chapter").drop(1)
		return chapters.map { chapter ->
			val url = chapter.substringAfter("href=\"").substringBefore("\">")
			val name = chapter.substringAfter("/\">").substringBefore("</a>")
			val chapNum = url.substringAfter("chapter-").substringBefore("-")
			val d = chapter.substringAfter("<i>").substringBefore("</i>")
			val dateText = if (d.contains("New")) {
				"today"
			} else {
				d
			}
			MangaChapter(
				id = generateUid(url),
				name = name,
				number = chapNum.toInt(),
				url = url,
				scanlator = null,
				uploadDate = parseChapterDate(
					dateFormat,
					dateText,
				),
				branch = null,
				source = source,
			)
		}
	}

	private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
	private fun parseChapters(root: Element): List<MangaChapter> {
		return root.select("li.wp-manga-chapter")
			.map { li ->
				val url = li.selectFirstOrThrow("a").attrAsRelativeUrl("href")
				val dateText = if (li.selectFirstOrThrow(".chapter-release-date").text() == "New") {
					"today"
				} else {
					li.selectFirstOrThrow(".chapter-release-date").text()
				}
				val chapNum = url.substringAfter("chapter-").substringBefore("-")

				MangaChapter(
					id = generateUid(url),
					name = li.selectFirstOrThrow("a").text(),
					number = chapNum.toInt(),
					url = url,
					scanlator = null,
					uploadDate = parseChapterDate(
						dateFormat,
						dateText,
					),
					branch = null,
					source = source,
				)
			}
	}

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		return doc.select(".reading-detail  img").map { img ->
			val url = img.src() ?: img.parseFailed("Image src not found")
			MangaPage(
				id = generateUid(url),
				url = url,
				preview = null,
				source = source,
			)
		}
	}

	private fun parseChapterDate(dateFormat: DateFormat, date: String?): Long {
		val d = date?.lowercase() ?: return 0
		return when {
			d.startsWith("today") -> Calendar.getInstance().apply {
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.SECOND, 0)
				set(Calendar.MILLISECOND, 0)
			}.timeInMillis

			else -> dateFormat.tryParse(date)
		}
	}
}
