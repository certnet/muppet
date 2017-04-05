package cn.bronzeware.util.testframework;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Unmodifiable;

import cn.bronzeware.muppet.util.FileUtil;

public class HtmlTestUnitStorage {

	private String path;
	public HtmlTestUnitStorage(String path){
		this.path = path;
	}
	
	
	
	public Map<String, File> initialize(Map<String, Map<String, TestUnitMetaData>> unitMaps,Map<String, TestUnitMetaData> unit){
		File file = new File(path);
		FileUtil.createFile(file);
		File[] files = file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return true;
			}
		});
		for(File f:files){
			f.delete();
		}
		
		Map<String, File> fileMap = new HashMap<>();
		int i = 0;
		for(Map.Entry<String, Map<String, TestUnitMetaData>> entry : unitMaps.entrySet()){
			Map<String, TestUnitMetaData> map = entry.getValue();
			String time = entry.getKey();
			fileMap.put(time, initializeHtml(map, unit, time));
		}
		initializeIndex(fileMap);
		
		return fileMap;
	}
	
	public File initializeIndex(Map<String, File> fileMap){
		PrintStream ps  = null;
		File file = null;
		try {
			file = new File(String.format("%s/index.html", path));
			FileUtil.createFile(file);
			FileOutputStream fos = new FileOutputStream(file);
			ps = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			
		}
		
		ps.println("<html>");
		ps.println("<body>");
		for(Map.Entry<String, File> entry:fileMap.entrySet()){
			ps.println(String.format("<a href='%s'>%s</a>", entry.getValue().getName()
					,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(entry.getKey())))));			
			ps.println("<br/><br/><br/>");
		}
		ps.println("</body>");
		ps.println("</html>");
		ps.close();
		return file;
	}
	
	public File initializeHtml(Map<String, TestUnitMetaData> map, Map<String, TestUnitMetaData> unit, String time){
		
		PrintStream ps  = null;
		File file = null;
		try {
			file = new File(String.format("%s/%s.html", path,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date(Long.parseLong(time)))));
			FileUtil.createFile(file);
			FileOutputStream fos = new FileOutputStream(file);
			ps = new PrintStream(fos);
		} catch (FileNotFoundException e) {
			
		}
		
		ps.println("<html>");
		ps.println("<body>");
		List<TestUnitMetaData> untracked = new ArrayList<TestUnitMetaData>();
		List<TestUnitMetaData> notModified = new ArrayList<>();
		List<TestUnitMetaData> modified = new ArrayList<>();
		List<TestUnitMetaData> deleted = new ArrayList<>();
		for(Map.Entry<String, TestUnitMetaData> entry:unit.entrySet()){
			String key = entry.getKey();
			TestUnitMetaData metaData = entry.getValue();
			if(! map.containsKey(key)){
				untracked.add(metaData);
				continue;
			}
			
			if(map.containsKey(key)){
				TestUnitMetaData m = map.get(key);
				if(m.equal(metaData)){
					notModified.add(metaData);
				}else{
					modified.add(metaData);
					modified.add(m);
				}
			}
			
		}
		
		for(Map.Entry<String, TestUnitMetaData> entry:map.entrySet()){
			String key = entry.getKey();
			TestUnitMetaData value = entry.getValue();
			if(!unit.containsKey(key)){
				deleted.add(value);
			}
		}
		
		ps.println("<h3>untracked</h3>");
		for(TestUnitMetaData m:untracked){
			initializePs(ps, m);
		}
		ps.println("<br/><br/><br/>");
		
		ps.println("<h3>deleted</h3>");
		for(TestUnitMetaData m:deleted){
			initializePs(ps, m);
		}
		ps.println("<br/><br/><br/>");
		
		ps.println("<h3>modified</h3>");
		int i = 0;
		for(TestUnitMetaData m:modified){
			if(i++%2==0){
				ps.println("<br/><br/><br/>");
			}
			initializePs(ps, m);
		}
		ps.println("<br/><br/><br/>");
		
		ps.println("<h3>unmodified</h3>");
		for(TestUnitMetaData m:notModified){
			initializePs(ps, m);
		}
		ps.println("<br/><br/><br/>");
		
		ps.println("</body>");
		ps.println("</html>");
		ps.close();
		return file;
	}
	
	private void initializePs(PrintStream ps, TestUnitMetaData m){
		ps.println("<table border=1>");
		ps.println("<tr>");
		ps.println(String.format("<td> className: %s </td>", m.getTargetClass().getName()));
		ps.println(String.format("<td> methodName : %s</td>", m.getMethodName()));
		ps.println("</tr>");
		
		ps.println("<tr>");
		ps.println("<td>returnValue</td>");
		ps.println(String.format("<td>%s</td>", m.getReturnValue()));
		ps.println("</tr>");
		
		ps.println("<tr>");
		ps.println(String.format("<td>CmdOut:</td>"));
		ps.println(String.format("<td>%s</td>", m.getCmdOutput()));
		ps.println("</tr>");
		
		ps.println("<tr>");
		ps.println(String.format("<td>CmdErr:</td>"));
		ps.println(String.format("<td>%s</td>", m.getCmdErr()));
		ps.println("</tr>");
		ps.println("</table>");
	}
	
}
