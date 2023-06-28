package org.koitharu.kotatsu.parsers.site.mangareader.id

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.mangareader.MangaReaderParser
import java.text.SimpleDateFormat
import java.util.*

@MangaSourceParser("MANGAINDO", "Mangaindo", "id")
internal class Mangaindo(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaSource.MANGAINDO, pageSize = 26, searchPageSize = 26) {
	override val configKeyDomain: ConfigKey.Domain
		get() = ConfigKey.Domain("mangaindo.me")

	override val listUrl: String
		get() = "/manga"
	override val tableMode: Boolean
		get() = false

	override val chapterDateFormat: SimpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)

}