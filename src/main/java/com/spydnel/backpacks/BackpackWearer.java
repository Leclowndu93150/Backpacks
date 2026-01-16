package com.spydnel.backpacks;

public interface BackpackWearer {

    void onBackpackOpen();
    void onBackpackClose();

    int getBackpackOpenCount();
    int getBackpackOpenTicks();
}
