package org.koitharu.kotatsu.parsers.site.madara.ar

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("NIJITRANSLATIONS", "Niji Translations", "ar")
internal class NijiTranslations(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.NIJITRANSLATIONS, "niji-translations.com") {

	override val postreq = true
}
