package org.koitharu.kotatsu.parsers.site.madara.ar


import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("ASQORG", "3Asq", "ar")
internal class Asq(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.ASQORG, "3asq.org") {

	override val datePattern = "d MMMM، yyyy"
}
