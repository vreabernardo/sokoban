fun cords(cords: String): String{
    val cds = cords.split(",")
    return "soko.png|${cds[0]},${cds[1]},40,54"         // X  = cds[0] | Y = cds[1]
}

val Sprites = mapOf(
    "sourceSize" to cords("241,272"),

    // Walk 1
    "UP0" to  cords("0,1"),
    "Right0" to  cords("0,55"),
    "Down0" to  cords("0,109"),
    "Left0" to cords("0,163"),

    // Static
    "UPS" to cords("40,1"),
    "RightS" to cords("40,55"),
    "DownS" to cords("40,109"),
    "LeftS" to cords("40,163"),

    // Walk 2
    "UP1" to  cords("81,1"),
    "Right1" to  cords("81,55"),
    "Down1" to  cords("81,109"),
    "Left1" to cords("81,163"),

    // Walk 1 while push
    "UP1P" to  cords("121,1"),
    "Right1P" to  cords("121,55"),
    "Down1P" to  cords("121,109"),
    "Left1P" to cords("121,163"),

    // Static while push
    "UPPS" to cords("160,1"),
    "RightPS" to cords("160,55"),
    "DownPS" to cords("160,109"),
    "LeftPS" to cords("160,163"),

    // Walk 2 while push
    "UP0P" to  cords("200,1"),
    "Right0P" to  cords("200,55"),
    "Down0P" to  cords("200,109"),
    "Left0P" to cords("200,163"),

    //Objects
    "Box" to cords("80,218"),
    "BoxOnTarget" to cords("120,218"),
    "Target" to cords("0,218"),
    "Wall" to cords("40,218")
)


