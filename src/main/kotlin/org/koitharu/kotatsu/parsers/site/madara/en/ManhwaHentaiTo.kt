package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHWAHENTAITO", "ManhwaHentai To", "en", ContentType.HENTAI)
internal class ManhwaHentaiTo(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANHWAHENTAITO, "manhwahentai.to", 10) {

	override val tagPrefix = "pornhwa-genre/"
	override val listUrl = "pornhwa/"
	override val datePattern = "d MMMM yyyy"
}
