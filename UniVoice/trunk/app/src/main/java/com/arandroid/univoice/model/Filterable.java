package com.arandroid.univoice.model;

import java.io.Serializable;

/**
 * Created by MeringoloRo on 09/01/2018.
 */

public interface Filterable extends Serializable {

    boolean isCompliant(String filter);
}
