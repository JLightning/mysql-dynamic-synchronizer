package com.jhl.mds.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseEvent<T> {

    private T data;
}
