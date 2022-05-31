package com.ask.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ask.coronavirustracker.models.LocationStats;
@Service
public class CoronaVirusDataService 
{
	private static String VIRUS_DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_US.csv";
	
	private List<LocationStats> allstats=new ArrayList<>();
	
	public List<LocationStats> getAllstats() {
		return allstats;
	}

	public void setAllstats(List<LocationStats> allstats) {
		this.allstats = allstats;
	}

	@PostConstruct
	@Scheduled(cron="* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException
	{
		List<LocationStats> newstats=new ArrayList<>();
		HttpClient client=HttpClient.newHttpClient();
		HttpRequest request =HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
		HttpResponse<String> httpResponse=client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(httpResponse.body());
		
		StringReader csvBodyReader=new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
		for (CSVRecord record : records) {
			LocationStats locationStat=new LocationStats();
			locationStat.setState(record.get("Province_State"));
			locationStat.setCountry(record.get("Country_Region"));
			
			int latestCases=Integer.parseInt(record.get(record.size()-1));
			int prevDayCases=Integer.parseInt(record.get(record.size()-2));
			
			locationStat.setLatestTotalCases(latestCases);
			locationStat.setDiffFromPrevDay(latestCases-prevDayCases);
			
			newstats.add(locationStat);
		}
		this.allstats=newstats;
	}
}
