package com.ola.fivethirtyeight.config

/*data class FeedConfig(
    val name: String,
    val url: String,
    val imageExtractor: (Element) -> String = ::extractImage,
    val titleTransformer: (String) -> String = { it }
)


 val TOP_STORY_FEEDS = listOf(
    FeedConfig(
        name = "ABC",
        url = "abcnews/topstories",
        imageExtractor = {
            it.getElementsByTag("media:thumbnail")
                .first()?.attr("url") ?: ""
        }
    ),
    FeedConfig(
        name = "Google",
        url = "https://news.google.com/rss/topics/CAAqJggKIiBDQkFTRWdvSUwyMHZNRFZxYUdjU0FtVnVHZ0pWVXlnQVAB?hl=en-US&gl=US&ceid=US%3Aen",
        imageExtractor = { extractGoogleImage(it) },
        titleTransformer = { it.substringBeforeLast("-") }
    ),
    FeedConfig(
        name = "NYT",
        url = "https://rss.nytimes.com/services/xml/rss/nyt/US.xml",
        imageExtractor = {
            it.getElementsByTag("media:content")
                .first()?.attr("url") ?: ""
        }
    ),

    FeedConfig(
        name = "NPR",
        url = "https://feeds.npr.org/1001/rss.xml",
        imageExtractor = {
            it.getElementsByTag("content:encoded")
                .first()
                ?.select("img")
                ?.attr("src")
                ?: ""
        }
    ),

            *//*FeedConfig (
            name = "CNN",
     url = "http://rss.cnn.com/rss/cnn_topstories.rss",
     imageExtractor = {
         it.getElementsByTag("content:encoded")
             .first()
             ?.select("img")
             ?.attr("src")
             ?: ""
     }
 ),
            *//*

     FeedConfig (
                    name = "WSJ",
     url = "http://online.wsj.com/xml/rss/3_ 7085.xml",
     imageExtractor = {
         it.getElementsByTag("content:encoded")
             .first()
             ?.select("img")
             ?.attr("src")
             ?: ""
     }
 )


)*/

val TOP_STORY_FEED_URLS = listOf(
"https://feeds.reuters.com/Reuters/PoliticsNews",
"https://feeds.reuters.com/reuters/businessNews",
"https://feeds.reuters.com/reuters/healthNews",
"https://feeds.reuters.com/reuters/scienceNews",
"https://feeds.reuters.com/reuters/technologyNews",
"https://feeds.reuters.com/reuters/entertainment",
"https://feeds.reuters.com/reuters/UKdomesticNews",
"https://feeds.reuters.com/reuters/INbusinessNews",
"https://hosted2.ap.org/atom/APDEFAULT/f70471f764144,b2fab526d39972d37b3",

"https://feeds.bbci.co.uk/news/world/rss.xml",
"https://feeds.bbci.co.uk/news/uk/rss.xml",
"https://feeds.bbci.co.uk/news/politics/rss.xml",
"https://feeds.bbci.co.uk/news/business/rss.xml",
"https://feeds.bbci.co.uk/news/technology/rss.xml",
"https://feeds.bbci.co.uk/news/science_and_environme,nt/rss.xml",
"https://feeds.bbci.co.uk/news/health/rss.xml",
"https://feeds.bbci.co.uk/news/entertainment_and_art,s/rss.xml",
"https://feeds.bbci.co.uk/news/world/us_and_canada/r,ss.xml",
"https://feeds.bbci.co.uk/news/world/asia/india/rss.,xml",
"https://feeds.bbci.co.uk/news/world/asia/rss.xml",

"https://rss.nytimes.com/services/xml/rss/nyt/Politi,cs.xml",
"https://rss.nytimes.com/services/xml/rss/nyt/Busine,ss.xml",
"https://rss.nytimes.com/services/xml/rss/nyt/Scienc,e.xml",
"https://rss.nytimes.com/services/xml/rss/nyt/Sports,.xml",
"https://rss.nytimes.com/services/xml/rss/nyt/Upshot,.xml",

"https://feeds.washingtonpost.com/rss/rss_wonkblog",
"https://feeds.washingtonpost.com/rss/rss_storyline",

"https://feeds.nbcnews.com/feeds/topstories",
"https://feeds.nbcnews.com/feeds/worldnews",
"https://feeds.nbcnews.com/feeds/usnews",
"https://feeds.nbcnews.com/feeds/health",
"https://feeds.nbcnews.com/feeds/todayentertainment",

"https://feeds.abcnews.com/abcnews/healthheadlines",

"https://www.npr.org/rss/rss.php?id=1014",
"https://www.npr.org/rss/rss.php?id=5",

"https://www.ft.com/rss/home/us",
"https://www.ft.com/rss/world/uk",
"https://www.ft.com/rss/world/asiapacific/india",

"https://www.theguardian.com/world/rss",
"https://feeds.theguardian.com/theguardian/politics/,rss",
"https://feeds.theguardian.com/theguardian/technolog,y/rss",

"https://news.sky.com/feeds/rss/uk.xml",

"https://feeds.harvardbusiness.org/harvardbusiness?f,ormat=xml",
"https://time.com/health/feed/",
"https://time.com/tech/feed/",
"https://www.newscientist.com/feed/home",
"https://khn.org/feed/",
"https://www.medpagetoday.com/rss/Headlines.xml",
"https://www.medscape.com/cx/rssfeeds/2700.xml",


"https://feeds.wired.com/wired/index",
"https://feeds.arstechnica.com/arstechnica/technolog,y-lab",
"https://feeds.arstechnica.com/arstechnica/gaming",
"https://feeds.venturebeat.com/VentureBeat",
"https://feeds.nasa.gov/rss/dyn/breaking_news.rss",
"https://feeds.sciencedaily.com/sciencedaily",
"https://feeds.sciencedaily.com/sciencedaily/top_new,s/top_health",

"https://feeds.feedburner.com/TechCrunch/",
"https://www.engadget.com/rss.xml",
"https://www.theverge.com/rss/index.xml",

"https://feeds.ign.com/ign/all",
"https://feeds.feedburner.com/RockPaperShotgun",
"https://toucharcade.com/feed/",
"https://www.polygon.com/rss/index.xml",

"https://www.reddit.com/r/politics/.rss",
"https://feeds.feedburner.com/realclearpolitics/qlMj",
"https://sports.yahoo.com/top/rss.xml",


)


