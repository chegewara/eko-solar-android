package eu.eko_solar.logistyka;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Dariusz Krempa on 2016-03-03.
 */
public class JobContent {

    public static List<Job> jobsList = new ArrayList<Job>();
    public static final TreeMap<String, Job> ITEM_MAP = new TreeMap<>();

    public static class Job {
        public String driverJobID;
        public String jobID;
        public String info;
        public String driverID;
        public String job_type;
        public String address;
        public String driverName;
        public String contact;
        public String cash;
        public String size;
        private JSONObject job = new JSONObject();

        public Job(String jsonJob) {

            try {
                job = new JSONObject(jsonJob);
                contact = job.getString("contact");
                address = job.getString("address_full");
                info = job.getString("info");
                job_type = job.getString("job_type");
                driverID = job.getString("driver");
                jobID = job.getString("jobID");
                cash = job.getString("cash");
                size = job.getString("size");
                driverJobID = job.getString("_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public static void addItem(Job item) {
        ITEM_MAP.put(item.driverJobID, item);
        jobsList = new ArrayList<Job>(ITEM_MAP.values());
    }

    public static void removeItem(String item) {
        ITEM_MAP.remove(item);
        jobsList = new ArrayList<>(ITEM_MAP.values());
    }
}
