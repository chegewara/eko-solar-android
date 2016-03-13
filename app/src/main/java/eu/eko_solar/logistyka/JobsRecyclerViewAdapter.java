package eu.eko_solar.logistyka;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.eko_solar.logistyka.JobsListFragment.OnJobListListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link JobContent.Job} and makes a call to the
 * specified {@link JobsListFragment.OnJobListListener}.
 */
public class JobsRecyclerViewAdapter extends RecyclerView.Adapter<JobsRecyclerViewAdapter.ViewHolder> {

    private final List<JobContent.Job> jobList;
    private final OnJobListListener mListener;

    public JobsRecyclerViewAdapter(List<JobContent.Job> items, OnJobListListener listener) {
        jobList = items;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.job_holder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.job = jobList.get(position);
        holder.address.setText(jobList.get(position).address);
        holder.contact.setText(jobList.get(position).contact);
        holder.type.setText(jobList.get(position).job_type);
        holder.size.setText(jobList.get(position).size);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onClickList(holder.job);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView address;
        public final TextView contact;
        private final TextView size;
        private final TextView type;
        public JobContent.Job job;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            address = (TextView) view.findViewById(R.id.address_item);
            contact = (TextView) view.findViewById(R.id.contact_item);
            type = (TextView) view.findViewById(R.id.type_item);
            size = (TextView) view.findViewById(R.id.size_item);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contact.getText() + "'";
        }
    }
}
