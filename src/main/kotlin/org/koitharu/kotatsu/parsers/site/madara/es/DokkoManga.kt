package org.koitharu.kotatsu.parsers.site.madara.es

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("DOKKOMANGA", "Dokko Manga", "es")
internal class DokkoManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.DOKKOMANGA, "dokkomanga.com", 10)
