package org.koitharu.kotatsu.parsers.site.madara.id

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser
import java.util.*

@MangaSourceParser("KOMIKSA", "Komiksay", "id")
internal class Komiksay(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.KOMIKSA, "komiksay.site") {

	override val tagPrefix = "komik-genre/"
	override val datePattern = "MMMM d"
	override val sourceLocale: Locale = Locale.ENGLISH
}