package com.example.ggxiaozhi.factory.presenter.group;


import com.example.ggxiaozhi.factory.Factory;
import com.example.ggxiaozhi.factory.R;
import com.example.ggxiaozhi.factory.data.DataSource;
import com.example.ggxiaozhi.factory.data.helper.GroupHelper;
import com.example.ggxiaozhi.factory.data.helper.UserHelper;
import com.example.ggxiaozhi.factory.model.api.group.GroupMemberAddModel;
import com.example.ggxiaozhi.factory.model.card.GroupMemberCard;
import com.example.ggxiaozhi.factory.model.db.view.MemberUserModel;
import com.example.ggxiaozhi.factory.model.db.view.UserSampleModel;
import com.example.ggxiaozhi.factory.presenter.BaseRecyclerPresenter;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工程名 ： ITalker
 * 包名   ： com.example.ggxiaozhi.factory.presenter.group
 * 作者名 ： 志先生_
 * 日期   ： 2017/12
 * 功能   ：
 */

public class GroupMemberAddPresenter extends BaseRecyclerPresenter<GroupCreateContract.ViewModel,
        GroupMemberAddContract.View> implements GroupMemberAddContract.Presenter,
        DataSource.Callback<List<GroupMemberCard>> {
    private Set<String> users = new HashSet<>();

    public GroupMemberAddPresenter(GroupMemberAddContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();

        // 加载可以添加的用户
        Factory.runOnAsync(loader);
    }

    @Override
    public void submit() {
        GroupMemberAddContract.View view = getView();
        view.showLoading();

        // 判断参数
        if (users.size() == 0) {
            view.showError(R.string.label_group_member_add_invalid);
            return;
        }

        // 进行网络请求
        GroupMemberAddModel model = new GroupMemberAddModel(users);
        GroupHelper.addMembers(view.getGroupId(), model, this);
    }

    @Override
    public void changeSelect(GroupCreateContract.ViewModel model, boolean isSelected) {
        if (isSelected)
            users.add(model.mAuthor.getId());
        else
            users.remove(model.mAuthor.getId());
    }

    private Runnable loader = new Runnable() {
        @Override
        public void run() {
            GroupMemberAddContract.View view = getView();
            if (view == null)
                return;

            // 我所有的联系人
            List<UserSampleModel> contact = UserHelper.getSampleContact();
            // 以及在群里的人
            List<MemberUserModel> members = GroupHelper.getMemberUsers(view.getGroupId(), -1);

            for (MemberUserModel member : members) {
                // 如果有就进行移除
                int index = indexOfUserContact(contact, member.getUserId());
                if (index >= 0)
                    contact.remove(index);
            }

            // 返回一个界面显示的Model
            List<GroupCreateContract.ViewModel> models = new ArrayList<>();
            for (UserSampleModel model : contact) {
                GroupCreateContract.ViewModel viewModel = new GroupCreateContract.ViewModel();
                viewModel.mAuthor = model;
                models.add(viewModel);
            }

            refreshData(models);
        }
    };

    // 联系人中找用户的位置坐标
    private int indexOfUserContact(List<UserSampleModel> contact, String userId) {
        int index = 0;
        for (UserSampleModel model : contact) {
            if (model.getId().equalsIgnoreCase(userId))
                return index;
            index++;
        }
        return -1;
    }

    @Override
    public void onDataLoaded(List<GroupMemberCard> groupMemberCards) {
        // 成功
        GroupMemberAddContract.View view = getView();
        if (view == null)
            return;
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                GroupMemberAddContract.View view = getView();
                if (view == null)
                    return;
                view.onAddedSucceed();
            }
        });
    }

    @Override
    public void onDataNotAvailable(final int strRes) {
        // 失败
        GroupMemberAddContract.View view = getView();
        if (view == null)
            return;
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                GroupMemberAddContract.View view = getView();
                if (view == null)
                    return;
                view.showError(strRes);
            }
        });
    }
}