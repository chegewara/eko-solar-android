package eu.eko_solar.logistyka;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import eu.eko_solar.logistyka.BoxGridFragment.OnBoxGridListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnBoxGridListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class BoxesRecyclerViewAdapter extends RecyclerView.Adapter<BoxesRecyclerViewAdapter.ViewHolder> {

    private final List<JobContent.Job> mValues;
    private final OnBoxGridListener mListener;

    public BoxesRecyclerViewAdapter(List<JobContent.Job> items, OnBoxGridListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(Integer.toString(position));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onBoxGridClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final Button mIdView;
        public JobContent.Job mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (Button) view.findViewById(R.id.id);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + "'";
        }
    }
}
