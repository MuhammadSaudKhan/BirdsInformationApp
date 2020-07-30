package com.material.recipe.connection.callbacks;

import com.material.recipe.model.Category;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CallbackListCategory implements Serializable {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public List<Category> categories = new ArrayList<>();

}
