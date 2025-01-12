package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGA_QUEEN", "Manga Queen", "en")
internal class MangaQueen(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANGA_QUEEN, "mangaqueen.com", 16)
