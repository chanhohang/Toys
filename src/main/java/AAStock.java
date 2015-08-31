import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AAStock implements Runnable
{
    private static boolean isDebug = false;
    
    private static int TAG_RANGE = 1000;
    private static int VALUE_RANGE = 50;

//    private final String chi_stock_url = "http://www.aastocks.com/tc/ltp/rtquote.aspx?symbol=";
    private final String eng_stock_url = "http://www.aastocks.com/en/LTP/RTQuote.aspx?&symbol=";
//    private final String eng_stock_url = "http://www.aastocks.com/en/stock/BasicQuote.aspx?&symbol=";
//    private final String eng_index_url = "http://www.aastocks.com/EN/market/HKIndex.aspx?Index=";
    private final String eng_index_url = "http://www.aastocks.com/en/stocks/market/index/hk-index-con.aspx?index=";
    
    private final DecimalFormat numFormat = new DecimalFormat("00000");
    
    public AAStock()
    {
        // Setup Proxy if Proxy is required.
//        System.getProperties().put("http.proxyHost", "someProxyURL");
//        System.getProperties().put("http.proxyPort", "someProxyPort");
//        System.getProperties().put("http.proxyUser", "someUserName");
//        System.getProperties().put("http.proxyPassword", "somePassword");
        
    }
    public byte[] getHTMLByte(URL url) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
        conn.connect();
        
        InputStream is = conn.getInputStream();
        
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String result = "";
        String line = null;
        while ((line = br.readLine() )!=null)
        {
            result += line + "\n";
        }
       
        return result.getBytes();
        
    }

    
    public static void main(String[] args)
    {
        
       AAStock aaStock = new AAStock();
       
       ExecutorService es = Executors.newSingleThreadExecutor();
       
       Future fut = es.submit(aaStock);
       
    }


    @Override
    public void run()
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line= null;
            while ((line=br.readLine()) != null)
            {
            	if (line.trim().length() == 0)
            	{
            		continue;
            	}
                if ("HSI".compareToIgnoreCase(line) == 0)
                {
                    onIndex("HSI");
                }
                else if ("HSCEI".compareToIgnoreCase(line) == 0)
                {
                    onIndex("HSCEI");
                }
                else
                {
                    onStockCode(line);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void onStockCode(String line) throws Exception
    {
        String targetURL = eng_stock_url + numFormat.format(Double.parseDouble(line));

        URL url = new URL(targetURL);

        byte[] result = getHTMLByte(url);
        StockQuoteData data = parseStockCodeHTMLByte(result);
        
        System.out.println(data);
    }


    protected void onIndex(String index) throws Exception
    {
        String targetURL = eng_index_url + index;

        URL url = new URL(targetURL);

        byte[] result = getHTMLByte(url);

        IndexQuoteData data = parseIndexHTMLByte(result, index);
        
        System.out.println(data);
    }

    private StockQuoteData parseStockCodeHTMLByte(byte[] result)
    {
        StockQuoteData data = new StockQuoteData();

        String htmlStr = new String(result);
        if (isDebug)
        {
            System.out.println(htmlStr);
        }
        boolean isPos = false;
        
        // Get Last
        String last_substring = htmlStr.substring(htmlStr.indexOf(("Last\n")));

        data.last = extractStr(last_substring, "Last\n", "<span class=\"neg bold\">", "</span>");
        if (data.last == null)
        {
            data.last = extractStr(last_substring, "Last\n", "<span class=\"pos bold\">", "</span>");
        }
        if (data.last == null)
        {
            data.last = extractStr(last_substring, "Last\n", "<span class=\"unc bold\">",  "</span>");
        }
        
        // Get Change
        isPos = false;
        data.chg = extractStr(last_substring, "Chg", "<span class=\"neg bold\">", "</span>");
        if (data.chg == null)
        {
            data.chg = extractStr(last_substring, "Chg", "<span class=\"pos bold\">", "</span>");
            isPos = true;
        }
        if (data.chg == null)
        {
            data.chg = extractStr(last_substring, "Chg", "<span class=\"unc bold\">",  "</span>");
        }
        data.chg = addSign(data.chg, isPos);
        
        // Get Change %
        isPos = false;
        data.chg_percent = extractStr(last_substring, "Chg(%)", "<span class=\"neg bold\">", "</span>");
        if (data.chg_percent == null)
        {
            data.chg_percent = extractStr(last_substring, "Chg(%)", "<span class=\"pos bold\">", "</span>");
            isPos = true;
        }
        if (data.chg_percent == null)
        {
            data.chg_percent = extractStr(last_substring, "Chg(%)", "<span class=\"unc bold\">",  "</span>");
        }
        data.chg_percent = addSign(data.chg_percent, isPos);
        
        // Get Volume
        data.volume = extractStr(htmlStr, "Volume", "<strong>", "</strong>");
        
        // Get Market Cap
        data.market_cap = extractStr(htmlStr, "Market Cap", "<strong>", "</strong>");

        // Get Turnover
        data.turnover = extractStr(htmlStr, "Turnover\n", "<strong>", "</strong>");

        // Get EPS
        data.EPS = extractStr(htmlStr, "EPS", "<strong>", "</strong>");

        // Get P/E Ratio
        data.PE = extractStr(htmlStr, "P/E Ratio", "<strong>", "</strong>");
        
        // Get Yield
        data.yield = extractStr(htmlStr, "Yield", "<strong>", "</strong>");
       
        // Get Lot Size
        data.lot_size = extractStr(htmlStr, "Lot Size", "<strong>", "</strong>");

        // Get Today's Range
        data.today_range= extractStr(htmlStr, "Range", "<strong>", "</strong>");

        // Get Today's Range
        data._52_week_range= extractStr(htmlStr, "52 WK", "<strong>", "</strong>");

        return data ;
    }

    private IndexQuoteData parseIndexHTMLByte(byte[] result, String index)
    {
        IndexQuoteData data = new IndexQuoteData();
        boolean isPos = false;
        String htmlStr = new String(result);
        if (isDebug)
        {
            System.out.println(htmlStr);
        }
        
        // Get Last
        data.last = extractStr(htmlStr, "ETFLast", "&nbsp;", "</span>");

        // Get Change
        isPos = false;
        data.chg = extractStr(htmlStr, "ETFChange", "<span class='neg'>", "</span>");
        if (data.chg == null)
        {
            data.chg = extractStr(htmlStr, "ETFChange", "<span class='pos'>", "</span>");
            isPos = true;
        }
        if (data.chg == null)
        {
            data.chg = extractStr(htmlStr, "ETFChange", "<span class='unc'\">",  "</span>");
        }

        // Get Turnover
        data.turnover = extractStr(htmlStr, "ETFTurnover", ">", "</div>");

        // Get High
        data.high = extractStr(htmlStr, "Range&nbsp;", "-", "</span>");

        // Get Low
        data.low = extractStr(htmlStr, "Range&nbsp;", ">", "-");

        // Get Open
        data.open = extractStr(htmlStr, "Open</div>", ">", "</div>");

        // Get Prev Close
        data.prevClose = extractStr(htmlStr, "Prev Close</div>", ">", "</div>");

        // Get 1 month range
        data.one_month_range = extractStr(htmlStr, "1-Month Range</td>", ">", "</td>");

        // Get 2 month range
        data.two_month_range = extractStr(htmlStr, "2-Month Range</td>", ">", "</td>");

        // Get 3 month range
        data.three_month_range = extractStr(htmlStr, "3-Month Range</td>", ">", "</td>");

        // Get 52 week range
        data.fiftytwo_week_range = extractStr(htmlStr, "52-Week Range</td>", ">", "</td>");
        
        return data;
    }


    private String addSign(String input, boolean isPos)
    {
        if (input == null)
        {
            return null;
        }
        
        if (isPos)
        {
            return "+" + input;
        }
        
        return "-" + input;
    }
    private String extractStr(String htmlStr, String startStr, String exactStartStr, String exactEndStr)
    {
        int startIndex = 0;
        int exactStartIndex = 0;
        int exactEndIndex = 0;
        
        // Get Last
        startIndex = htmlStr.indexOf(startStr);
        if (startIndex == -1)
        	return null;
        
        startIndex += startStr.length();
        
        exactStartIndex = htmlStr.indexOf(exactStartStr, startIndex) + exactStartStr.length();
        exactEndIndex = htmlStr.indexOf(exactEndStr, exactStartIndex);
        
        if (exactStartIndex - startIndex > TAG_RANGE || exactStartIndex < startIndex)
        {
        	return null;
        }
        
        String result = htmlStr.substring(exactStartIndex, exactEndIndex).trim();
        
    	int innerStart = result.lastIndexOf(">");
    	if (innerStart > -1)
    	{
    		result = result.substring(innerStart+1).trim();
    	}
        
        if (result.length() > VALUE_RANGE)
        	return null;
        
        return result;
    }
    
    public static class IndexQuoteData
    {
        public String last;
        public String chg;
        public String turnover;
        public String high;
        public String low;
        public String open;
        public String prevClose;
        public String one_month_range;
        public String two_month_range;
        public String three_month_range;
        public String fiftytwo_week_range;
        
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("==============================\n");
            sb.append("Last      = " + last + "\n");
            sb.append("Change    = " + chg + "\n");
            sb.append("Turnover  = " + turnover + "\n");
            sb.append("High (" + high + ") Low (" + low + ")\n");
            sb.append("Open (" + open + ") prevClose (" + prevClose + ")\n");
            sb.append("1 Month Range (" + one_month_range + ")\n" + "2 Month Range (" + two_month_range + ")\n");
            sb.append("3 Month Range (" + three_month_range + ")\n" + "52 Week Range (" + fiftytwo_week_range + ")\n");
            sb.append("==============================");

            return sb.toString();
        }
    }
    
    public static class StockQuoteData
    {
        public String last;
        public String chg;
        public String chg_percent;
        public String volume;
        public String market_cap;
        public String turnover;
        public String EPS;
        public String PE;
        public String yield;
        public String lot_size;
        public String today_range;
        public String _52_week_range;
        
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("==============================\n");
            sb.append("Last      = " + last + "\n");
            sb.append("Change    = " + chg + " (" + chg_percent + ")\n");
            sb.append("Today     = " + today_range + "\n");
            sb.append("52 Week   = " + _52_week_range + "\n");
            sb.append("Volume (" + volume + ") MarketCap (" + market_cap + ") Turnrover (" + turnover + ")\n");
            sb.append("EPS (" + EPS+ ") PE (" + PE + ") Yield (" + yield + ") Lot Size (" + lot_size + ")\n");
            sb.append("==============================");
            return sb.toString();
        }
    }
}
