package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHWAFULL", "ManhwaFull", "en")
internal class ManhwaFull(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANHWAFULL, "manhwafull.com") {

	override val listUrl = "manga-all-manhwa/"
	override val datePattern = "MM/dd/yyyy"
}
