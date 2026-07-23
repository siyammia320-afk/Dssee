package com.example.network

enum class TargetCategory(val title: String) {
    GAME_SERVERS("Game Servers"),
    DNS_PROVIDERS("DNS Providers"),
    GLOBAL_CDN("Global CDNs"),
    CUSTOM("Custom Host")
}

data class ServerTarget(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 80,
    val region: String,
    val iconName: String,
    val category: TargetCategory
)

object DefaultServerTargets {
    val servers = listOf(
        // Game Servers
        ServerTarget("pubg_asia", "PUBG Mobile (Asia)", "107.155.48.1", 80, "Asia-Pacific", "gamepad", TargetCategory.GAME_SERVERS),
        ServerTarget("mlbb_sea", "Mobile Legends (SEA)", "128.199.200.1", 80, "Southeast Asia", "gamepad", TargetCategory.GAME_SERVERS),
        ServerTarget("valorant_tokyo", "Valorant (Tokyo)", "151.101.1.140", 80, "Asia-Pacific", "gamepad", TargetCategory.GAME_SERVERS),
        ServerTarget("fortnite_eu", "Fortnite (EU Frankfurt)", "52.28.63.252", 80, "Europe", "gamepad", TargetCategory.GAME_SERVERS),
        ServerTarget("roblox_us", "Roblox (US West)", "128.116.119.1", 80, "North America", "gamepad", TargetCategory.GAME_SERVERS),
        ServerTarget("steam_global", "Steam Master Server", "162.254.195.1", 80, "Global", "gamepad", TargetCategory.GAME_SERVERS),

        // DNS Providers
        ServerTarget("cloudflare_dns", "Cloudflare DNS", "1.1.1.1", 53, "1.1.1.1", "dns", TargetCategory.DNS_PROVIDERS),
        ServerTarget("google_dns", "Google Public DNS", "8.8.8.8", 53, "8.8.8.8", "dns", TargetCategory.DNS_PROVIDERS),
        ServerTarget("quad9_dns", "Quad9 Secure DNS", "9.9.9.9", 53, "9.9.9.9", "dns", TargetCategory.DNS_PROVIDERS),
        ServerTarget("opendns", "Cisco OpenDNS", "208.67.222.222", 53, "208.67.222.222", "dns", TargetCategory.DNS_PROVIDERS),
        ServerTarget("adguard_dns", "AdGuard DNS", "94.140.14.14", 53, "94.140.14.14", "dns", TargetCategory.DNS_PROVIDERS),

        // Global CDNs & Services
        ServerTarget("google_com", "Google Edge", "google.com", 80, "Nearest Edge", "cloud", TargetCategory.GLOBAL_CDN),
        ServerTarget("cloudflare_cdn", "Cloudflare Edge", "104.16.132.229", 80, "Global Edge", "cloud", TargetCategory.GLOBAL_CDN),
        ServerTarget("fastly_cdn", "Fastly CDN", "151.101.1.69", 80, "Global CDN", "cloud", TargetCategory.GLOBAL_CDN),
        ServerTarget("aws_us_east", "AWS US-East", "dynamodb.us-east-1.amazonaws.com", 80, "US-East", "cloud", TargetCategory.GLOBAL_CDN)
    )
}
