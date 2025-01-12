package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAGG", "Mangagg", "en")
internal class Mangagg(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANGAGG, "mangagg.com") {

	override val tagPrefix = "genre/"
	override val datePattern = "MM/dd/yyyy"
}
