package org.koitharu.kotatsu.parsers.site.madara.tr

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser
import java.util.Locale

@MangaSourceParser("MANWE", "Manwe", "tr")
internal class Manwe(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANWE, "manwe.pro", 20) {

	override val datePattern = "MMMM d, yyyy"
	override val sourceLocale: Locale = Locale("tr")

}
