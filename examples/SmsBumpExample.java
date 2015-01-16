import com.smsbump.SmsBump;
import com.smsbump.SmsBumpRunnable;

public class SmsBumpExample {
	public static void main(String[] args) throws Exception {
		SmsBump smsbump = new SmsBump("{your_api_key}}", "{recipient(s)}", "Hello World from SmsBump!");
        smsbump.setCallback(new SmsBumpRunnable() {
            private String response;
            
            public void setResponse(String response) { this.response = response; }

            public void run() {
                System.out.println( this.response );
            }
        });
		smsbump.send();
	}
}