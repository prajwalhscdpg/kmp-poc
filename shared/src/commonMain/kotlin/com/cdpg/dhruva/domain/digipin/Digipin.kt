package com.cdpg.dhruva.domain.digipin

/**
 * DIGIPIN, India Post's 10-character geocode. A direct port of the official reference
 * implementation (github.com/INDIAPOST-gov/digipin, `src/digipin.js`), via Dhruva iOS's
 * EncodeDigipinUseCase: same 16-symbol grid, bounding box and per-level partitioning.
 * Pure Kotlin, so it works offline and gives identical results on Android and iOS.
 */
object Digipin {

    private val GRID = arrayOf(
        charArrayOf('F', 'C', '9', '8'),
        charArrayOf('J', '3', '2', '7'),
        charArrayOf('K', '4', '5', '6'),
        charArrayOf('L', 'M', 'P', 'T'),
    )

    const val MIN_LAT = 2.5
    const val MAX_LAT = 38.5
    const val MIN_LON = 63.5
    const val MAX_LON = 99.5
    const val LENGTH = 10

    /** The raw 10-character code (no separators), or null outside DIGIPIN coverage. */
    fun encode(latitude: Double, longitude: Double): String? {
        if (latitude !in MIN_LAT..MAX_LAT || longitude !in MIN_LON..MAX_LON) return null

        var minLat = MIN_LAT
        var maxLat = MAX_LAT
        var minLon = MIN_LON
        var maxLon = MAX_LON
        val code = StringBuilder(LENGTH)

        repeat(LENGTH) {
            val latDiv = (maxLat - minLat) / 4
            val lonDiv = (maxLon - minLon) / 4

            val row = (3 - ((latitude - minLat) / latDiv).toInt()).coerceIn(0, 3)
            val col = ((longitude - minLon) / lonDiv).toInt().coerceIn(0, 3)

            code.append(GRID[row][col])

            maxLat = minLat + latDiv * (4 - row)
            minLat += latDiv * (3 - row)
            minLon += lonDiv * col
            maxLon = minLon + lonDiv
        }
        return code.toString()
    }

    /**
     * The centre of the cell a code names, or null if it isn't a valid code.
     * Accepts the display form too ("39J-438-TJC7").
     */
    fun decode(code: String): Pair<Double, Double>? {
        val clean = code.replace("-", "").replace(" ", "").uppercase()
        if (clean.length != LENGTH) return null

        var minLat = MIN_LAT
        var maxLat = MAX_LAT
        var minLon = MIN_LON
        var maxLon = MAX_LON

        for (char in clean) {
            val row = GRID.indexOfFirst { char in it }
            if (row < 0) return null
            val col = GRID[row].indexOf(char)

            val latDiv = (maxLat - minLat) / 4
            val lonDiv = (maxLon - minLon) / 4
            val lat1 = maxLat - latDiv * (row + 1)
            val lat2 = maxLat - latDiv * row
            val lon1 = minLon + lonDiv * col
            val lon2 = lon1 + lonDiv
            minLat = lat1
            maxLat = lat2
            minLon = lon1
            maxLon = lon2
        }
        return Pair((minLat + maxLat) / 2, (minLon + maxLon) / 2)
    }

    /** Grouped 3-3-4 for display, e.g. "39J-438-TJC7" (India Post's readable form). */
    fun formatForDisplay(code: String): String =
        if (code.length == LENGTH) "${code.substring(0, 3)}-${code.substring(3, 6)}-${code.substring(6)}" else code
}
