package cn.charlotte.pit.mode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Mode {

    Myhic("Mythic"," "),
    Normal("Normal","RPGItem");

    public final String name;
    public final String internal;
}
