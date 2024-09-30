# Subway Live Map

This app fetches updates from the [MTA](https://new.mta.info/) data feed via an open source REST API, [Transiter](https://docs.transiter.dev/), continuously finding the earliest arrivals for every subway stop in New York City. It also tracks the exponential moving average of wait times for every arrival, smoothing out spikes in the data while still giving a visual representation of average wait times, bottlenecks, and delays. Each station on the map is colored by the average of all of that station's arrival wait times, with times closer to 0 minutes being bluer, and those closer to 20+ as redder. 

For tracking arrival times, it seems generally accurate and in-line with other apps like https://realtimerail.nyc/, which uses the same API on the backend. However, after running this all night, I noticed a bug where the southbound arrivals for one particular line appeared "stuck" in the past, despite the raw data feed from the MTA outputting as normal. There are probably some other minor bugs here and there, but the major stuff seems to be working.

~~If I get around to it, I want to add the ability to search for stops and filter by line color, as well as connecting the nodes on the map to those on the list (i.e., clicking either also highlights the other).~~

- [x] Clicking a map circle highlights the corresponding station card in the subway list
- [ ] Clicking a station card in the subway list highlights the corresponding map circle
- [ ] Add a search box for stations
- [ ] Add a filter for lines
- [ ] Add a legend
- [ ] Display each exponential moving average for each line on each station card
- [ ] Predict future times with machine learning?? 🤔