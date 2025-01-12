package org.koitharu.kotatsu.parsers.site.mangabox.en

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.model.*
import org.koitharu.kotatsu.parsers.site.mangabox.MangaboxParser

@MangaSourceParser("MANGANELO_COM", "MangaNelo Com", "en")
internal class MangaNeloCom(context: MangaLoaderContext) :
	MangaboxParser(context, MangaSource.MANGANELO_COM) {
	override val configKeyDomain = ConfigKey.Domain("m.manganelo.com", "chapmanganelo.com")
	override val otherDomain = "chapmanganelo.com"
}
