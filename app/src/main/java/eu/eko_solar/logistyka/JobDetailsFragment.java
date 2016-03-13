package eu.eko_solar.logistyka;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.app.Fragment;

import static eu.eko_solar.logistyka.MainActivity.lat;
import static eu.eko_solar.logistyka.MainActivity.lng;


/**
 * A simple {@link Fragment} subclass.
 */
public class JobDetailsFragment extends Fragment implements View.OnClickListener{

    private JobContent.Job job;
    View view;
    private TextView address;
    private TextView cash;
    private Context context;

    public JobDetailsFragment() {
    }


    @Override
    public void onStart() {
        super.onStart();
        view = getView();
        if(view != null){
            address = (TextView) view.findViewById(R.id.address_detail);
            TextView contact = (TextView) view.findViewById(R.id.contact_details);
            TextView job_type = (TextView) view.findViewById(R.id.job_type_details);
            cash = (TextView) view.findViewById(R.id.cash_details);
            TextView info = (TextView) view.findViewById(R.id.info_details);
            TextView size = (TextView) view.findViewById(R.id.size_details);

            address.setText(job.address);
            contact.setText(job.contact);
            job_type.setText(job.job_type);
            cash.setText(job.cash);
            size.setText(job.size);
            info.setText(job.info);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = inflater.getContext();
        View layout = inflater.inflate(R.layout.fragment_job_details, container, false);
        Button button = (Button) layout.findViewById(R.id.ended_details);
        Button button1 = (Button) layout.findViewById(R.id.not_ended_details);
        Button button2 = (Button) layout.findViewById(R.id.navi_details);

        button.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        return layout;
    }

    public void setJob(JobContent.Job item){
        this.job = item;
    }

    @Override
    public void onClick(View v) {
        final String[] data = new String[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog dialog;
        switch (v.getId()){
            case R.id.not_ended_details: //TODO dialog powód,
                String reason = "";
                data[0] = "{\"id\":" + job.driverJobID + ", \"reason:\"" + reason + "}";
                MyBroadcastReceiver.socket.emitEvent("notEndJob", data[0]);
                break;

            case R.id.ended_details:
                final AlertDialog finalDialog[] = new AlertDialog[1];
                builder.setTitle(R.string.end_job_dialog)
                        .setView(inflater.inflate(R.layout.end_job, null))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dia, int id) {
                                EditText cashDialog = (EditText) finalDialog[0].findViewById(R.id.cash_dialog_end_job);
                                EditText boxDialog = (EditText) finalDialog[0].findViewById(R.id.box_dialog_end_job);
                                data[0] = "{\"id\":\"" + job.driverJobID + "\", \"cash2\":" + cashDialog.getText() + ", \"box\":{\"number\":" + boxDialog.getText() + ", \"position\":{\"lat\":"+ lat + ", \"lng\":" + lng + "}}, \"executed\":true}";
                                MyBroadcastReceiver.socket.emitEvent("endJob", data[0]);
                                JobContent.removeItem(job.driverJobID);
                                ((MyBroadcastReceiver.MyMsgSocket) context).newJobMsg();
                                if (JobContent.jobsList.size() > 0)
                                    ((JobsListFragment.OnJobListListener) context).onClickList(JobContent.jobsList.get(0));
                                else
                                    ((JobsListFragment.OnJobListListener) context).onClickList(null);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                dialog = builder.create();
                dialog.show();
                finalDialog[0] = dialog;
                EditText cashDialog = (EditText) dialog.findViewById(R.id.cash_dialog_end_job);
                cashDialog.setText(cash.getText());
                EditText boxDialog = (EditText) dialog.findViewById(R.id.box_dialog_end_job);
                boxDialog.setText("0");

                break;

            case R.id.navi_details: //TODO adres lub location
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+address.getText());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
                break;
        }
    }
}
