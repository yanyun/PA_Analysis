import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.splunk.Args;
import com.splunk.Job;
import com.splunk.ResultsReaderXml;
import com.splunk.Service;
import com.splunk.ServiceArgs;

class SplunkConnection{
	String userName;
	String password;
	String host;
	int port;
	
	public SplunkConnection(String userName, String password, String host, int port){
		this.userName = userName;
		this.password = password;
		this.host = host;
		this.port = port;
	}
	
	public Service getServiceConnection(){
		ServiceArgs loginArgs = new ServiceArgs();
		
		loginArgs.setUsername(userName);
		loginArgs.setPassword(password);
		loginArgs.setHost(host);
		loginArgs.setPort(port);
		
		Service service = Service.connect(loginArgs);
		
		return service;
	}
}



public class PA_Analysis{
	static String[] inputPara;
	
	public static String searchCommand;
	public static void main(String args[]) throws ParseException{
		
		String searchid = args[0];
		inputPara = searchid.trim().split("\\,"); // Now inputPara[] = { pf/uat, jobid1 starttime1, jobid2 startid2}
		
		
		Parameters para = new Parameters();		
		
		
		SplunkConnection splunkConnection = null;
		
		if(inputPara[0].equals("uat")){
			splunkConnection = new SplunkConnection("bol1","bol1","ueu-b1-wcal0002.coresit.msci.org",8089);
			searchCommand = "index=varlog" + ", " + inputPara[1] + ", " + inputPara[2];
		}else{
			splunkConnection = new SplunkConnection("bol1","bol1","10.10.100.248",8089);
			searchCommand = "index=" + inputPara[0] + ", " + inputPara[1] + ", " + inputPara[2];
		}
		
		para.getPara(searchCommand);
		para.getParaList();


		Service splunkService = splunkConnection.getServiceConnection();
		
        Args oneshotSearchArgs_01 = new Args();
        oneshotSearchArgs_01.put("earliest", para.start_01);
        oneshotSearchArgs_01.put("latest", para.end_01);
        String oneshotSearchQuery_01 = "search " + para.index +" "+ para.session_01 +" "+"\"PerfStats\" |" + " table "+"ptag avg num min max tot";

        Args oneshotSearchArgs_02 = new Args();
        oneshotSearchArgs_02.put("earliest", para.start_02);
        oneshotSearchArgs_02.put("latest", para.end_02);
        String oneshotSearchQuery_02 = "search " + para.index +" "+ para.session_02 +" "+ "\"PerfStats\" |"+" table "+"ptag avg num min max tot";
       
        InputStream results_oneshot_01 =  splunkService.oneshotSearch(oneshotSearchQuery_01, oneshotSearchArgs_01);
        InputStream results_oneshot_02 =  splunkService.oneshotSearch(oneshotSearchQuery_02, oneshotSearchArgs_02);
        
        Job job_01 = splunkService.getJobs().create(oneshotSearchQuery_01, oneshotSearchArgs_01);
        Job job_02 = splunkService.getJobs().create(oneshotSearchQuery_02, oneshotSearchArgs_02);
        
    	Map<String, ArrayList<Object>> resultMap_01 = new HashMap<String, ArrayList<Object>>();	
    	Map<String, ArrayList<Object>> resultMap_02 = new HashMap<String, ArrayList<Object>>();
    	
        System.out.println("Connecting Splunk...");
        while( (!job_01.isDone()) && (!job_02.isDone())){
        	try{
        		Thread.sleep(500);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        System.out.println("Splunk connection is successful");
        
        try{
        		ResultsReaderXml resultsReader_01 = new ResultsReaderXml(results_oneshot_01);
            	HashMap<String, String> event_01;
            	
            	ResultsReaderXml resultsReader_02 = new ResultsReaderXml(results_oneshot_02);
            	HashMap<String, String> event_02;


            	while((event_01 = resultsReader_01.getNextEvent()) != null){
            		ArrayList<Object> arr_01 = new ArrayList<Object>();
            		arr_01.add(event_01.get("ptag"));
            		arr_01.add(event_01.get("avg"));
            		arr_01.add(event_01.get("num"));
            		arr_01.add(event_01.get("min"));
            		arr_01.add(event_01.get("max"));
            		arr_01.add(event_01.get("tot"));
            		resultMap_01.put(event_01.get("ptag"), arr_01);
            	}
            	
            	while((event_02 = resultsReader_02.getNextEvent()) != null){
            		ArrayList<Object> arr_02 = new ArrayList<Object>();
            		arr_02.add(event_02.get("ptag"));
            		arr_02.add(event_02.get("avg"));
            		arr_02.add(event_02.get("num"));
            		arr_02.add(event_02.get("min"));
            		arr_02.add(event_02.get("max"));
            		arr_02.add(event_02.get("tot"));
            		resultMap_02.put(event_02.get("ptag"), arr_02);
            	}
            	
            	Map<String, ArrayList<Object>> resultMap = new HashMap<String, ArrayList<Object>>();
            	
            	for(String key_01 : resultMap_01.keySet()){
            		for(String key_02 : resultMap_02.keySet()){
            			ArrayList<Object> arr = new ArrayList<Object>();
            			arr.addAll(resultMap_01.get(key_01));
            			arr.add(" ");
            			arr.addAll(resultMap_02.get(key_02));
            			arr.add(" ");
            			if(resultMap_01.get(key_01).get(resultMap_01.get(key_01).size()-1) != null && resultMap_02.get(key_02).get(resultMap_02.get(key_02).size()-1) != null){
            				double tot1 = Integer.parseInt((String) resultMap_01.get(key_01).get(resultMap_01.get(key_01).size()-1));
            				double tot2 = Integer.parseInt((String) resultMap_02.get(key_02).get(resultMap_02.get(key_02).size()-1));
            				int a = (int) (tot1 - tot2);
            				if(tot1 != 0){
            					double b = a/tot1;
                				DecimalFormat df = new DecimalFormat("0.00%");
                				String s = df.format(b);
                				arr.add(s);
            				}          				
            				arr.add(a);
            			}
//            			arr.add(Integer.parseInt(resultMap_01.get(key_01).get(resultMap_01.get(key_01).size()-1).toString()));
            			if (key_01!=null && key_02!=null && key_01.equals(key_02)){
//            				System.out.println(d++);            				
            				resultMap.put(key_01, arr);
            			}/*else if(key_01 == null){
            				resultMap.put(key_02, arr);
            			}else if(key_02 == null){
            				resultMap.put(key_01, arr);
            			}*/
            		}
            		
            	}
            	
            	Map<Integer, ArrayList<Object>> map = new TreeMap<Integer, ArrayList<Object>>();
            	ArrayList<String> titleList = new ArrayList<String>();
            	titleList.add("ptag");
            	titleList.add("avg");
            	titleList.add("num");
            	titleList.add("min");
            	titleList.add("max");
            	titleList.add("tot");
            	titleList.add(" ");
            	titleList.add("ptag");
            	titleList.add("avg");
            	titleList.add("min");
            	titleList.add("min");
            	titleList.add("tot");
            	titleList.add("tot");
            	titleList.add(" ");
            	titleList.add("Percent Diff");
            	titleList.add("Relative Diff");

            	for(String key : resultMap.keySet()){
/*            		for(int i = 0; i < resultMap.get(key).size(); i++){
            			//System.out.print(resultMap.get(key).get(i) + " ");
            		}
//            		System.out.println();
*/            		
            		map.put((Integer) resultMap.get(key).get(resultMap.get(key).size() - 1), resultMap.get(key));
            	}
            	
            	/*for(Integer key : map.keySet()){
            		for(int i = 0; i < map.get(key).size(); i++){
//            			System.out.print(map.get(key).get(i) + " ");
            		}
//            		System.out.println();
            		
            		//map.put((Integer) resultMap.get(key).get(resultMap.get(key).size() - 1), resultMap.get(key));
            	}*/
            	

            	
            	DoExcel doExcel = new DoExcel(map);
            	doExcel.getTitle(titleList);
            	doExcel.createExcel();

            	splunkService.logout();

        }catch(Exception e){
        	e.printStackTrace();
        }
       
	}
}
