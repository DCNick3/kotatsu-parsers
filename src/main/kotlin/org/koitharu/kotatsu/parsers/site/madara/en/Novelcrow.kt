package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("NOVELCROW", "NovelCrow", "en", ContentType.HENTAI)
internal class Novelcrow(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.NOVELCROW, "novelcrow.com", 24) {

	override val tagPrefix = "comic-genre/"
}
