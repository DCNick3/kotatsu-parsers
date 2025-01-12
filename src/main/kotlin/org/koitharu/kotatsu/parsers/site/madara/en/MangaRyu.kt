package org.koitharu.kotatsu.parsers.site.madara.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.ContentType
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGARYU", "Manga Ryu", "en", ContentType.HENTAI)
internal class MangaRyu(context: MangaLoaderContext) :
	MadaraParser(context, MangaSource.MANGARYU, "mangaryu.com", 10)
