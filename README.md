# Subway Live Map

Fetches updates from the MTA data feed and finds the earliest arrivals for every stop. Tracks the exponential moving average of wait times for every arrival and displays this on a map. This smooths out spikes in the data while still giving a visual representation of average wait times, bottlenecks, and delays. Wait times closer to 0 minutes are blue, while those closer to 20+ are red.

For tracking upcoming arrival times, it seems generally accurate and in-line with other apps like https://realtimerail.nyc/, which use the same API on the backend. However, after running this all night, I noticed a bug where the southbound arrivals for one particular line appeared "stuck" in the past, despite the raw data feed from the MTA outputting as normal. There are probably some other minor bugs here and there, but the major stuff seems to be working.

If I get around to it, I want to add the ability to search for stops and filter by line color, as well as connecting the nodes on the map to those on the list (i.e., clicking either also highlights the other).