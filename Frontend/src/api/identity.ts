export type UserProfileModel = {
  userProfileId: string
  userProfileAccountId: string
  userProfileFirstName: string | null
  userProfileLastName: string | null
  userProfileDateOfBirth: string | null
  userProfileGender: "Unspecified" | "Male" | "Female"
  userProfilePhoneNumber: string | null
  userProfileAddress: string | null
  userProfileAvatarUrl: string | null
}

export type PermissionModel = {
  permissionId: string
  permissionCode: string
  permissionName: string
  permissionDescription: string | null
  permissionIsActive: boolean
  permissionCreatedAt: string | null
  permissionUpdatedAt: string | null
}

export type RoleModel = {
  roleId: string
  roleCode: string
  roleName: string
  roleDescription: string | null
  roleIsActive: boolean
  roleCreatedAt: string | null
  roleUpdatedAt: string | null
  permissions: PermissionModel[]
}

export type AccountModel = {
  accountId: string
  accountEmail: string
  accountIsActive: boolean
  accountCreatedAt: string | null
  accountUpdatedAt: string | null
  roles: RoleModel[]
  additionalPermissions: PermissionModel[]
  userProfile: UserProfileModel | null
  roleCodes: string[]
  permissionCodes: string[]
}

export type CursorPage<T> = {
  items: T[]
  nextCursor: string | null
  isHasMore: boolean
}

export type RecordAuthResponse = {
  accountId: string
  email: string
  isActive: boolean
  roleCodes: string[]
  permissionCodes: string[]
  accountCreatedAt: string | null
  accountUpdatedAt: string | null
}

export type RecordLoginResponse = {
  account: RecordAuthResponse
  profile: UserProfileModel | null
  claims: { type: string; value: string }[]
}

type BackendAccountResponse = {
  accountId: string
  email: string
  active: boolean
  roleCodes: string[]
  permissionCodes: string[]
  createdAt: string | null
  updatedAt: string | null
}

type BackendRoleResponse = {
  roleId: string
  code: string
  name: string
  description: string | null
  active: boolean
  createdAt?: string | null
  updatedAt?: string | null
  permissions?: BackendPermissionResponse[]
}

type BackendPermissionResponse = {
  permissionId: string
  code: string
  name: string
  description: string | null
  active: boolean
  createdAt?: string | null
  updatedAt?: string | null
}

type BackendProfileResponse = {
  identityCode: string
  accountId: string
  firstName: string | null
  lastName: string | null
  phoneNumber: string | null
  address: string | null
  avatarUrl: string | null
  dateOfBirth: string | null
  gender: UserProfileModel["userProfileGender"]
}

type BackendLoginResponse = {
  account: BackendAccountResponse
  profile: BackendProfileResponse | null
  claims: { type: string; value: string }[]
}

type BackendCursorPage<T> = {
  items: T[]
  nextCursor: string | null
  hasMore: boolean
}

const apiBase = (import.meta.env.VITE_IDENTITY_API_BASE_URL || "/api").replace(/\/$/, "")

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    const headers = new Headers(init?.headers)
    if (init?.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json")
    response = await fetch(`${apiBase}${path}`, {
      ...init,
      headers,
    })
  } catch {
    throw new Error(`Không thể kết nối Identity API tại ${apiBase}. Kiểm tra Spring Boot có chạy ở cổng 8081 không.`)
  }

  if (!response.ok) {
    const body = await response.text()
    let detail = body
    try {
      const problem = JSON.parse(body) as { detail?: string; message?: string; title?: string }
      detail = problem.detail || problem.message || problem.title || body
    } catch {
      // Spring có thể trả lỗi dạng plain text.
    }
    throw new Error(detail || `Identity API trả về HTTP ${response.status}.`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

function toAccountModel(account: BackendAccountResponse): AccountModel {
  return {
    accountId: account.accountId,
    accountEmail: account.email,
    accountIsActive: account.active,
    accountCreatedAt: account.createdAt,
    accountUpdatedAt: account.updatedAt,
    roleCodes: account.roleCodes ?? [],
    permissionCodes: account.permissionCodes ?? [],
    roles: [],
    additionalPermissions: [],
    userProfile: null,
  }
}

function toAuthResponse(account: BackendAccountResponse): RecordAuthResponse {
  return {
    accountId: account.accountId,
    email: account.email,
    isActive: account.active,
    roleCodes: account.roleCodes ?? [],
    permissionCodes: account.permissionCodes ?? [],
    accountCreatedAt: account.createdAt,
    accountUpdatedAt: account.updatedAt,
  }
}

function toRoleModel(role: BackendRoleResponse): RoleModel {
  return {
    roleId: role.roleId,
    roleCode: role.code,
    roleName: role.name,
    roleDescription: role.description,
    roleIsActive: role.active,
    roleCreatedAt: role.createdAt ?? null,
    roleUpdatedAt: role.updatedAt ?? null,
    permissions: role.permissions?.map(toPermissionModel) ?? [],
  }
}

function toPermissionModel(permission: BackendPermissionResponse): PermissionModel {
  return {
    permissionId: permission.permissionId,
    permissionCode: permission.code,
    permissionName: permission.name,
    permissionDescription: permission.description,
    permissionIsActive: permission.active,
    permissionCreatedAt: permission.createdAt ?? null,
    permissionUpdatedAt: permission.updatedAt ?? null,
  }
}

function toProfileModel(profile: BackendProfileResponse | null): UserProfileModel | null {
  if (!profile) return null
  return {
    userProfileId: profile.identityCode,
    userProfileAccountId: profile.accountId,
    userProfileFirstName: profile.firstName,
    userProfileLastName: profile.lastName,
    userProfileDateOfBirth: profile.dateOfBirth,
    userProfileGender: profile.gender,
    userProfilePhoneNumber: profile.phoneNumber,
    userProfileAddress: profile.address,
    userProfileAvatarUrl: profile.avatarUrl,
  }
}

function accountPagePath(cursor: string | null, pageSize: number, status: "all" | "active" | "inactive") {
  const path = status === "all" ? "/account/analytics" : "/account/status"
  const query = new URLSearchParams({ pageSize: String(pageSize), isGetProfile: "true" })
  if (cursor) query.set("cursor", cursor)
  if (status !== "all") query.set("isActive", String(status === "active"))
  return `${path}?${query}`
}

async function fetchAccountPage(cursor: string | null, pageSize: number, status: "all" | "active" | "inactive") {
  const page = await request<BackendCursorPage<BackendAccountResponse>>(accountPagePath(cursor, pageSize, status))
  return {
    items: page.items.map(toAccountModel),
    nextCursor: page.nextCursor,
    isHasMore: page.hasMore,
  }
}

export const identityApi = {
  accounts(cursor: string | null, pageSize: number, status: "all" | "active" | "inactive") {
    return fetchAccountPage(cursor, pageSize, status)
  },
  async roles() {
    const roles = await request<BackendRoleResponse[]>("/authorization/roles")
    return roles.map(toRoleModel)
  },
  async roleWithPermissions(roleCode: string) {
    const role = await request<BackendRoleResponse & { permissions: BackendPermissionResponse[] }>(
      `/authorization/roles/${encodeURIComponent(roleCode)}/permissions`,
    )
    return { ...toRoleModel(role), permissions: role.permissions.map(toPermissionModel) }
  },
  async permissions() {
    const permissions = await request<BackendPermissionResponse[]>("/authorization/permissions")
    return permissions.map(toPermissionModel)
  },
  async profileByAccountId(accountId: string) {
    const query = new URLSearchParams({ accountId })
    const profile = await request<BackendProfileResponse>(`/profile/profile?${query}`)
    return toProfileModel(profile)
  },
  async login(email: string, password: string): Promise<RecordLoginResponse> {
    const result = await request<BackendLoginResponse>("/account/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    })
    return {
      account: toAuthResponse(result.account),
      profile: toProfileModel(result.profile),
      claims: result.claims ?? [],
    }
  },
  async register(email: string, password: string): Promise<RecordAuthResponse> {
    const account = await request<BackendAccountResponse>("/account/register", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    })
    return toAuthResponse(account)
  },
  updateAccountRoles(accountId: string, roleIds: string[]) {
    return request<number>(`/authorization/accounts/${encodeURIComponent(accountId)}/roles`, {
      method: "PUT",
      body: JSON.stringify({ ids: roleIds }),
    })
  },
  updateRole(role: RoleModel, permissionIds: string[]) {
    return request<number>(`/authorization/roles/${encodeURIComponent(role.roleId)}`, {
      method: "PUT",
      body: JSON.stringify({
        code: role.roleCode,
        name: role.roleName,
        description: role.roleDescription,
        active: role.roleIsActive,
        permissionIds,
      }),
    })
  },
  async activateAccount(email: string) {
    const account = await request<BackendAccountResponse>("/account/active", {
      method: "POST",
      headers: { "Content-Type": "text/plain" },
      body: email,
    })
    return toAccountModel(account)
  },
  async deactivateAccount(accountId: string) {
    const account = await request<BackendAccountResponse>(`/account/delete/${encodeURIComponent(accountId)}`, {
      method: "DELETE",
    })
    return toAccountModel(account)
  },
}
