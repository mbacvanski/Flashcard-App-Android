package com.example.marc.materialtabviews.deck_chooser;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.marc.materialtabviews.Downloader;
import com.example.marc.materialtabviews.MainActivity;
import com.example.marc.materialtabviews.OnTaskCompleted;
import com.example.marc.materialtabviews.R;
import com.example.marc.materialtabviews.card_quiz.CardQuizFragment;
import com.example.marc.materialtabviews.misc_fragments.EnterCodeFragment;
import com.example.marc.materialtabviews.model.Deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckChooserFragment extends ListFragment implements OnTaskCompleted {

    public static final String DECK_LIST_SHEET_KEY = "1Zs0ydpL1twVTgUNi_h9b4KHRafrTMnUljOorwdfBm8I";
    private static final String TAG = "DeckChooserFragment";
    private ArrayList<Deck> deckList = new ArrayList<>();
    private ArrayList<String> deckNameList = new ArrayList<>();

    public DeckChooserFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Removes the view below it, such that this view does not
        // appear on top of the previous one.
        if (container != null) {
            container.removeAllViews();
        }

        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.deckChooserTitle));

        // Inflate the card_quiz_fragment inside container
        // This is very important to call.
        return inflater.inflate(R.layout.deck_chooser_fragment, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setEmptyView(getActivity().findViewById(android.R.id.empty));
        setListAdapter();

        ImageView emptyImage = (ImageView) getView().findViewById(android.R.id.empty);
        emptyImage.setImageResource(R.drawable.no_network);

        // This is the key to the lookup spreadsheet.
        // It contains deck names and their corresponding keys.
        String key = DECK_LIST_SHEET_KEY;

        // This is the file where we will save the JSON of the lookup spreadsheet.
        String fileName = Downloader.APP_DIRECTORY + Downloader.DECK_JSON;

        // This is used for the OnTaskCompleted interface
        // When it completes everything, it calls onTaskCompleted
        // And passes the arrayList of Deck as data.
        DeckChooserDownloader downloader = new DeckChooserDownloader(key, fileName, this);
        downloader.execute();
    }

    protected void setListAdapter() {
        // Set the adapter of the view to the schoolNameList
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, deckNameList);
        setListAdapter(adapter);
    }

    /**
     * Woot! We have a click!
     * I don't think l, v, or position matter much right now.
     *
     * @param l        ListView where the click happened
     * @param v        View that was clicked within the ListView
     * @param position Position of the view in the list
     * @param id       Row id of the clicked item
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
        Deck selection = deckList.get((int) id); // Gets the clicked place.
        String key = selection.getKey(); // Gets the key of the clicked place.
        String correctCode = selection.getCode();

        // TODO Look into using bundles
        CardQuizFragment cardQuiz = new CardQuizFragment();
        cardQuiz.setName(selection.getName());
        cardQuiz.setKey(selection.getKey()); // Sets the key.
        cardQuiz.setCode(selection.getCode());

        // Here, ask for the code
        EnterCodeFragment codeAuth = new EnterCodeFragment();

        codeAuth.setPrevious(this);
        codeAuth.setNext(cardQuiz);
        codeAuth.setCorrectCode(correctCode);
        codeAuth.setTitle(selection.getName());

        // We can get a fragment manager from the Fragment superclass
        FragmentManager fragmentManager = getFragmentManager();

        // We do the transaction, replacing the container with
        // the code fragment and then committing.
        fragmentManager.beginTransaction()
                .replace(R.id.container, codeAuth)
                .commit();
    }

    @Override
    /**
     * We assume that the ArrayList<Object> that we get
     * is of type Deck, for purposes of re-usability of the
     * OnTaskCompleted interface.
     *
     * @param data The result of the method that calls onTaskCompleted
     */
    public void onTaskCompleted(Boolean err, List<Object> data) {
        if (data.size() > 0) {
            deckList.clear();
            deckNameList.clear();

            // Populate the schoolNameList array with names of the decks.
            // We still will need the schoolList itself for the lookup
            // of names and keys inside onListItemClick.
            for (Object each : data) {
                deckList.add((Deck) each);
            }

            // Because we like our users.
            Collections.sort(deckList);

            // Now, both the deckList and deckNameList will be sorted.
            for (Deck each : deckList) {
                deckNameList.add(each.getName());
            }

            // Create a new adapter with schoolNameList.
            // TODO Maybe look into casting into a BaseAdapter, and then calling something like
            // TODO notifyDataSetChanged().
            try {
                setListAdapter();
            } catch (NullPointerException npe) {
                Log.w(TAG, "No data received from deckdownloader.");
                npe.printStackTrace();
            }
        } else {
            Log.i(TAG, "About to show snackbar");
            Snackbar snackbar = Snackbar
                    .make(getView(), "Network error! Yikes!", Snackbar.LENGTH_LONG);
            snackbar.show();


            Log.i(TAG, "Showed snackbar!");
        }
    }
}
