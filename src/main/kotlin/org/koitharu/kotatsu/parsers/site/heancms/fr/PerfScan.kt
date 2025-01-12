package org.koitharu.kotatsu.parsers.site.heancms.fr

import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.model.MangaSource
import org.koitharu.kotatsu.parsers.site.heancms.HeanCms

@MangaSourceParser("PERF_SCAN", "Perf Scan", "fr")
internal class PerfScan(context: MangaLoaderContext) :
	HeanCms(context, MangaSource.PERF_SCAN, "perf-scan.fr")
