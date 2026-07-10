package com.studies.rrbmustudies.domain.model

/**
 * Built-in home carousel slides — shown when Firebase ads are empty or unavailable.
 * Home & courses UI does not depend on Firestore.
 */
object HomeDefaults {
    val promoAds: List<HomeAd> = listOf(
        HomeAd(
            id = "builtin_exam_reg",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBFM542ga_1a3QjbddPVUclz2fv2shCxui-7a2df2snJFZy-gy64i8bQ2Bj-cgF5YhIk3laX0j9u9-UVe_tfgJwY09ZojJxmEAGGigUr8xuR-27ReD0r0d6Wunnupu7aIdb9tC26TOCKGdnMa6P9vuf02SI_dulDlvLzBy69ugEZTeI3bDPSFm7pAI-uRHtpJ40rbpwQd13bLiAswbkvpdKSZAaKjLoxdz13aoThdyfA-nwDZ-hg_Uj",
            title = "Exam Registration Open",
            description = "Register before Sept 30th for 2024-25",
            linkUrl = "https://rrbmuonline.com",
            order = 1,
        ),
        HomeAd(
            id = "builtin_library",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA9deNc_MvsD9GXSO8cpITFLamCfJjxc7V8TxL5lSIxXn-wuhue-nOklJdD4bXX0G-ZMX2TzcH42YKRIGUOZfgK0rxDj-kEdJITLq9oQk4qfD7Qw0SSixUCDZmIkZjbKnFSW3j0IKMhIja6Pd1pp44C0_kpEmV6ForXFdCGtCUvVR6g5ZgLpR6v9rICKjkpi928cfO7Y63RTQXxZFTMndntRYzxFOgeP1ePIeCWOcmBrsYws93hnNYR",
            title = "Digital Library Access",
            description = "Previous year question papers for RRBMU",
            linkUrl = "https://rrbmuonline.com",
            order = 2,
        ),
        HomeAd(
            id = "builtin_alumni",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCZPZ2sIDSe4z48xuHul6uqD2dhydPMPEoOpQUR5FR_ZrjsXWRK2X2zqbY6hfrRVlK63UU7W6eGr5-NX--CAuq4b2eoHXXZCLlsAKQ1kWXPP_oWaw_VImDDnONajZL8WcvYm4kJO63BorP35jCmDeY9HSowc9HAqjbbdVG6Zyn2_miTxDwiHtpjZ217OiCJoMVigwR8xLzk8KMyuk0oz9SxYdHT05ha5uwXy9ErqOim34QqhnvmGl-D",
            title = "RRBMU Studies",
            description = "Download BSC, BA, BCOM & more exam papers",
            linkUrl = "https://rrbmuonline.com",
            order = 3,
        ),
    )
}
