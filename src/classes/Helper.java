package classes;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {
	public String unicodeToASCII(String s) {
		String s1 = Normalizer.normalize(s, Normalizer.Form.NFKD);
		String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
		String s2 = null;
		try {
			s2 = new String(s1.replaceAll(regex, "").getBytes("ascii"), "ascii");
		} catch (Exception e) {
			return "";
		}
		
		return s2;
	}
	
	public Date parseStringToDate(String dateString, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		try {
			return (Date)dateFormat.parse(dateString);
		} catch(Exception e) {
			return null;
		}
	}
	
	public String parseDateToString(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		try {
			return dateFormat.format(date);
		} catch(Exception e) {
			return null;
		}
	}
	
	public int compareDate(Date date1, Date date2, String format) {
		Date d1 = this.parseStringToDate(this.parseDateToString(date1, format), format);
		Date d2 = this.parseStringToDate(this.parseDateToString(date2, format), format);
		return d1.equals(d2) ? 0 : d1.after(d2) ? 1 : -1;
	}
}
