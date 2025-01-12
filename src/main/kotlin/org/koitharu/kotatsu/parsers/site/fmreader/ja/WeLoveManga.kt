package org.koitharu.kotatsu.parsers.site.fmreader.ja


import org.jsoup.nodes.Document
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaChapter
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.fmreader.FmreaderParser
import org.koitharu.kotatsu.parsers.util.*
import java.text.SimpleDateFormat


@MangaSourceParser("WELOVEMANGA", "WeLoveManga", "ja")
internal class WeLoveManga(context: MangaLoaderContext) :
	FmreaderParser(context, MangaSource.WELOVEMANGA, "welovemanga.one") {

	override suspend fun getChapters(manga: Manga, doc: Document): List<MangaChapter> {
		val mid = doc.selectFirstOrThrow("div.cmt input").attr("value")
		val docload =
			webClient.httpGet("https://$domain/app/manga/controllers/cont.Listchapter.php?mid=$mid").parseHtml()
		val dateFormat = SimpleDateFormat(datePattern, sourceLocale)
		return docload.body().select(selectChapter).mapChapters(reversed = true) { i, a ->
			val href = a.selectFirstOrThrow("a").attrAsRelativeUrl("href")
			val dateText = a.selectFirst(selectDate)?.text()
			MangaChapter(
				id = generateUid(href),
				name = a.selectFirstOrThrow("a").text(),
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

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()

		val cid = doc.selectFirstOrThrow("#chapter").attr("value")
		val docload = webClient.httpGet("https://$domain/app/manga/controllers/cont.listImg.php?cid=$cid").parseHtml()
		return docload.select("img").map { img ->
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
