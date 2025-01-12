package org.koitharu.kotatsu.parsers.site.madara.pt

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("ILLUSIONSCAN", "Illusion Scan", "pt", ContentType.HENTAI)
internal class IllusionScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.ILLUSIONSCAN, "illusionscan.com") {

	override val datePattern: String = "dd 'de' MMMMM 'de' yyyy"
}
