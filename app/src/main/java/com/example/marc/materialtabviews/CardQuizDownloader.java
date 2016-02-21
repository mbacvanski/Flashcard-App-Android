package com.example.marc.materialtabviews;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marc on 152812.
 */
public class CardQuizDownloader extends Downloader {

    private DeckWithContents deckWithContents = null;

    public CardQuizDownloader(String name, String key, String code, String fileName, OnTaskCompleted completionWaiter) {
        super(fileName, completionWaiter, key);
        deckWithContents = new DeckWithContents(name, key, code, null);
    }

    @Override
    // Actually, this runs on the **main** thread
    // Weee!
    protected void onPostExecute(String result) {

        CardQuizReader reader = new CardQuizReader(getFileName());
        ArrayList<Card> cardList = new ArrayList<>();

        while (reader.hasNext()) {
            Card thisCard = (Card) reader.getNext();
            cardList.add(thisCard);
        }

        deckWithContents.setCards(cardList);
        List<Object> resultDeckList = new ArrayList<>();
        resultDeckList.add(deckWithContents);
        completionWaiter.onTaskCompleted(resultDeckList);
    }

}
