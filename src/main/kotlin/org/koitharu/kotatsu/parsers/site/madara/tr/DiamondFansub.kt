package org.koitharu.kotatsu.parsers.site.madara.tr


import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser
import java.util.Locale


@MangaSourceParser("DIAMONDFANSUB", "Diamond Fansub", "tr")
internal class DiamondFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.DIAMONDFANSUB, "diamondfansub.com", 10) {

	override val datePattern = "d MMMM"
	override val sourceLocale: Locale = Locale("tr")
	override val tagPrefix = "seri-turu/"
}
