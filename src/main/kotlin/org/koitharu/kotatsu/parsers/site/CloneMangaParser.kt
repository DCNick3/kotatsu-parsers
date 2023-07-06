package org.koitharu.kotatsu.parsers.site

import org.koitharu.kotatsu.parsers.InternalParsersApi
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaParser
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.parsers.model.RATING_UNKNOWN
import org.koitharu.kotatsu.parsers.model.SortOrder
import org.koitharu.kotatsu.parsers.util.attrAsAbsoluteUrl
import org.koitharu.kotatsu.parsers.util.attrAsAbsoluteUrlOrNull
import org.koitharu.kotatsu.parsers.util.domain
import org.koitharu.kotatsu.parsers.util.generateUid
import org.koitharu.kotatsu.parsers.util.parseFailed
import org.koitharu.kotatsu.parsers.util.parseHtml
import org.koitharu.kotatsu.parsers.util.selectFirstOrThrow
import org.koitharu.kotatsu.parsers.util.styleValueOrNull
import org.koitharu.kotatsu.parsers.util.toAbsoluteUrl
import java.util.Collections

@MangaSourceParser("CLONEMANGA", "CloneManga", "en")
internal class CloneMangaParser(context: MangaLoaderContext) : MangaParser(context, MangaSource.CLONEMANGA) {

	override val sortOrders: Set<SortOrder> = Collections.singleton(
		SortOrder.POPULARITY,
	)

	override val configKeyDomain = ConfigKey.Domain("manga.clone-army.org")

	@InternalParsersApi
	override suspend fun getList(offset: Int, query: String?, tags: Set<MangaTag>?, sortOrder: SortOrder): List<Manga> {
		if (query != null || offset > 0) {
			return emptyList()
		}
		val link = "https://${domain}/viewer_landing.php"
		val doc = webClient.httpGet(link).parseHtml()
		val mangas = doc.getElementsByClass("comicPreviewContainer")
		return mangas.mapNotNull { item ->
			val background = item.selectFirstOrThrow(".comicPreview").styleValueOrNull("background")
			val href = item.selectFirst("a")?.attrAsAbsoluteUrl("href") ?: return@mapNotNull null
			val cover = background?.substring(background.indexOf("site/themes"), background.indexOf(")"))
			Manga(
				id = generateUid(href),
				title = item.selectFirst("h3")?.text() ?: return@mapNotNull null,
				coverUrl = "https://${domain}/$cover",
				altTitle = null,
				author = "Dan Kim",
				rating = RATING_UNKNOWN,
				url = href,
				isNsfw = false,
				tags = emptySet(),
				state = null,
				publicUrl = href.toAbsoluteUrl(domain),
				source = source,
			)
		}
	}

	override suspend fun getDetails(manga: Manga): Manga {
		val doc = webClient.httpGet(manga.publicUrl).parseHtml()
		val series = doc.location()
		val numChapters = Regex(
			pattern = "&page=(.*)&lang=",
		).findAll(
			input = doc.getElementsByTag("script")[3].toString(),
		)
			.elementAt(3).destructured.component1()
			.toInt()
		val chapters = ArrayList<MangaChapter>()
		for (i in 0..numChapters) {
			val chapter = MangaChapter(
				id = generateUid("$series&page=$i"),
				name = "Chapter ${i + 1}",
				number = i + 1,
				url = "$series&page=$i",
				scanlator = null,
				branch = null,
				uploadDate = 0L,
				source = source,
			)
			chapters.add(chapter)
		}
		return manga.copy(chapters = chapters)
	}

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val doc = webClient.httpGet(chapter.url.toAbsoluteUrl(domain)).parseHtml()
		val imgUrl = doc.getElementsByClass("subsectionContainer")[0]
			.selectFirst("img")
			?.attrAsAbsoluteUrlOrNull("src") ?: doc.parseFailed("Something broken")
		return listOf(
			MangaPage(
				id = generateUid(imgUrl),
				url = imgUrl,
				preview = null,
				source = source,
			),
		)
	}

	override suspend fun getTags(): Set<MangaTag> = emptySet()
}
