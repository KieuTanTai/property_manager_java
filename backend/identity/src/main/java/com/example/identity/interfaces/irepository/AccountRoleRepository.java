package com.example.identity.interfaces.irepository;

import com.example.identity.models.account.AccountRoleModel;
import com.example.shared.interfaces.BaseAssociativeRepository;
import java.util.UUID;

public interface AccountRoleRepository extends BaseAssociativeRepository<AccountRoleModel, UUID> {}
