package com.deadlinesaver.android.recyclerview;

public interface onMoveAndSwipeListener {

    /**
     * 数据交换
     * @param fromPos 从
     * @param toPos 到
     */
    void onItemMove(int fromPos, int toPos);

    /**
     * 数据删除
     * @param position 位置
     */
    void onItemRemove(int position);

}
