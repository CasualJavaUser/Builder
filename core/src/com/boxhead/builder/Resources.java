package com.boxhead.builder;

public enum Resources {
        NOTHING(0),
        WOOD(1);

        private final int index;

        Resources(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
}
