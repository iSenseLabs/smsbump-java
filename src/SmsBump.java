package com.smsbump;

import java.net.*;
import java.io.*;
import java.util.*;

public class SmsBump implements Runnable {
    public static enum Type {
        SMS("sms"), VMS("vms"), USSD("ussd"), WHATSAPP("whatsapp");
        private String type;
        Type(String type) {
            this.type = type;
        }
        public String getString() {
            return type;
        }
    }
    public static enum Method {
        SEND("send"), ESTIMATE("estimate"), BALANCE("balance");
        private String method;
        Method(String method) {
            this.method = method;
        }
        public String getString() {
            return method;
        }
    }
    public boolean multithreading = true;

    private List < Thread > threadPool = new ArrayList < Thread > ();
    private static final int THREAD_MAX_COUNT = 5;

    private Method method = Method.SEND;
    private Type type = Type.SMS;

    private String APIKey = "";
    private String from = "";
    private String message = "";
    private Deque < String > to = new ArrayDeque < String > ();
    private List < String > responses = new ArrayList < String > ();

    private static String urlFormat = "?from=%s&to=%s&message=%s&type=%s";

    private SmsBumpRunnable callback;

    public SmsBump(String APIKey, String recipient, String message) {
        setAPIKey(APIKey);
        addRecipient(recipient);
        setMessage(message);
    }

    public SmsBump(String APIKey, String[] recipients, String message) {
        setAPIKey(APIKey);
        addRecipients(recipients);
        setMessage(message);
    }

    public SmsBump(String APIKey, String recipient, String message, SmsBumpRunnable callback) {
        setAPIKey(APIKey);
        addRecipient(recipient);
        setMessage(message);
        setCallback(callback);
    }

    public SmsBump(String APIKey, String[] recipients, String message, SmsBumpRunnable callback) {
        setAPIKey(APIKey);
        addRecipients(recipients);
        setMessage(message);
        setCallback(callback);
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCallback(SmsBumpRunnable callback) {
        this.callback = callback;
    }

    public void clearRecipients() {
        to.clear();
    }

    public void addRecipient(String recipient) {
        to.add(recipient);
    }

    public void addRecipients(String[] recipients) {
        to.addAll(Arrays.asList(recipients));
    }

    public String[] getResponses() {
        return responses.toArray(new String[responses.size()]);
    }

    public void send() throws Exception {
        responses.clear();
        int initSize = to.size();

        for (int x = 0; x < initSize; x++) {
            if (multithreading) {
                while (threadPool.size() == THREAD_MAX_COUNT) {
                    List < Thread > readyThreads = new ArrayList < Thread > ();
                    for (Thread thread: threadPool) {
                        if (thread.getState() == Thread.State.TERMINATED) readyThreads.add(thread);
                    }
                    for (Thread thread: readyThreads) {
                        threadPool.remove(thread);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //
                    }
                }
                Thread t = new Thread(this);
                threadPool.add(t);
                t.start();
            } else {
                run();
            }
        }
    }

    public void run() {
        String to = this.to.pollFirst();
        if (to != null) {
            try {
                String url = getApiUrl();
                if (method == Method.SEND || method == Method.ESTIMATE) {
                    url += String.format(urlFormat, from, to, message, type.getString());
                }

                URL smsBump = new URL(url);
                InputStreamReader in = new InputStreamReader(smsBump.openStream(), "UTF-8");

                StringBuilder sb = new StringBuilder();
                char[] buf = new char[1];

                while ( in .read(buf, 0, 1) != -1) {
                    sb.append(buf[0]);
                }; in .close();
                String response = sb.toString();
                responses.add(response);
                callback.setResponse(response);
                (new Thread(callback)).start();
            } catch (Exception e) {
                //do nothing for now
            }
        }
    }

    private String getApiUrl() {
        return "http://api.smsbump.com/" + method.getString() + "/" + APIKey + ".json";
    }
}