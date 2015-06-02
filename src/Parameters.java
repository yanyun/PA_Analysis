import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import com.splunk.Job;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;

public class Parameters{
	public String para;
	public String str;
	
	public String index;
	
	public String firstJobId;
	public String time_01;
	public String start_01;
	public String end_01;
	public String session_01;
	
	static String secondJobId;
	public String time_02;
	public String start_02;
	public String end_02;
	public String session_02;
	
	public String excelTitle;
	
	public String query;
	
	public void getPara(String para){
		this.para = para;	
	}
	
	public String[] getInfo(String jobId, String time) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy:HH:mm:ss");
		
		String start = time + ":00:00:00";
		Date startDate = df.parse(start);
		Calendar gc = new GregorianCalendar();
		gc.setTime(startDate);
		gc.add(Calendar.DAY_OF_YEAR, 5);
		Date endDate = (Date) gc.getTime();
		String startTime = df.format(startDate);
		String endTime = df.format(endDate);
		System.out.println("This start time is: "+ startTime);
		System.out.println("This end time is: "+ endTime);
		
		SplunkConnection splunkConnection = null;
		
		if(PA_Analysis.inputPara[0].equals("uat")){
			splunkConnection = new SplunkConnection("bol1","bol1","ueu-b1-wcal0002.coresit.msci.org",8089);
			query = "search index=varlog " +"earliest=\"" + startTime + "\" " + "latest=\"" + endTime + "\" " + jobId + " Report,Login";
			System.out.println(query);
		}else{
			splunkConnection = new SplunkConnection("bol1","bol1","10.10.100.248",8089);
			query = "search index=" + PA_Analysis.inputPara[0] + " earliest=\"" + startTime + "\" " + "latest=\"" + endTime + "\" " + jobId + " Report,Login";
			System.out.println(query);
		}

		Service service = splunkConnection.getServiceConnection();

		Job job = service.getJobs().create(query);
		
//		System.out.println(query);
		
		System.out.println("Connecting Splunk...");
		
		while (! job.isDone()) {
		    try {
		        Thread.sleep(6000);
		    } catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		}
		
		System.out.println("Splunk connection is successful");
		
		InputStream resultsNormalSearch =  job.getResults();

		ResultsReaderXml resultsReaderNormalSearch;
		
		
		try {
		    resultsReaderNormalSearch = new ResultsReaderXml(resultsNormalSearch);
		    HashMap<String, String> event;
		    while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
		        for (String key: event.keySet()){
//		        	System.out.print(event.get(key));
		            if(key.equals("_raw")){
		            	str = event.get(key);
		            }

		        }
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
//		System.out.println(str);
		
		String[] arrayStr = str.trim().split("\\,");
		String session_id = arrayStr[arrayStr.length - 1];

		
		
		
		String[] info = {session_id, startTime.toString(), endTime.toString()};
		
		service.logout();
		
		return info;
	}
	
	public void getParaList() throws ParseException{
		String[] resStr = para.trim().split("\\,");
		LinkedList<String> paraList = new LinkedList<String>();
		
		for(int i = 0; i < resStr.length; i++){
			paraList.add(resStr[i]);  // paraList = { index=pf/uat, jobid1 time1, jobid2,time2}
		}
		
		index = paraList.poll().trim();
		String[] job1 = paraList.poll().trim().split(" ");
		String[] job2 = paraList.poll().trim().split(" ");
		firstJobId = job1[0];
		time_01 = job1[1];
		secondJobId = job2[0];
		time_02 = job2[1];
		
//		System.out.println(firstJobId);
		
		String[] info_01 = this.getInfo(firstJobId, time_01);
		String[] info_02 = this.getInfo(secondJobId, time_02);
		
		session_01 = info_01[0];
		start_01 = info_01[1];
		end_01 = info_01[2];
		
		session_02 = info_02[0];
		start_02 = info_02[1];
		end_02 = info_02[2];
		
//		System.out.println(index+" "+start_01+" "+end_01+" "+session_01+" "+start_02+" "+end_02+" "+session_02+" "+ perfStats + " " + resData);
	}
	
	
	
}