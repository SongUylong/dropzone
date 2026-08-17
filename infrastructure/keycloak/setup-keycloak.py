import json
import urllib.request
import urllib.parse
import sys

BASE_URL = "http://localhost:8089"

def get_admin_token():
    url = f"{BASE_URL}/realms/master/protocol/openid-connect/token"
    data = urllib.parse.urlencode({
        "client_id": "admin-cli",
        "username": "admin",
        "password": "admin",
        "grant_type": "password"
    }).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={"Content-Type": "application/x-www-form-urlencoded"})
    with urllib.request.urlopen(req) as resp:
        res = json.loads(resp.read().decode("utf-8"))
        return res["access_token"]

def make_request(url, method="GET", body=None, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            if resp.status in (200, 201):
                content = resp.read().decode("utf-8")
                return json.loads(content) if content else {}
            return {}
    except urllib.error.HTTPError as e:
        err_msg = e.read().decode("utf-8")
        if e.code == 409: # Resource already exists
            print(f"Resource already exists at {url}")
            return None
        print(f"HTTP Error {e.code} for {method} {url}: {err_msg}")
        raise e

def setup_keycloak():
    token = get_admin_token()
    print("Obtained Admin Token.")

    # 1. Create Realm 'dropzone'
    realm_url = f"{BASE_URL}/admin/realms"
    realm_body = {
        "realm": "dropzone",
        "enabled": True,
        "accessTokenLifespan": 3600
    }
    make_request(realm_url, method="POST", body=realm_body, token=token)
    print("Realm 'dropzone' created or already exists.")

    # 2. Create Roles in 'dropzone'
    roles = ["USER", "ORGANIZER", "SUPPORT", "ADMIN"]
    roles_url = f"{BASE_URL}/admin/realms/dropzone/roles"
    for role in roles:
        make_request(roles_url, method="POST", body={"name": role}, token=token)
        print(f"Role '{role}' created or verified.")

    # 3. Create Client 'dropzone-api'
    clients_url = f"{BASE_URL}/admin/realms/dropzone/clients"
    client_body = {
        "clientId": "dropzone-api",
        "enabled": True,
        "publicClient": True,
        "directAccessGrantsEnabled": True,
        "standardFlowEnabled": True,
        "redirectUris": ["*"],
        "webOrigins": ["*"]
    }
    make_request(clients_url, method="POST", body=client_body, token=token)
    print("Client 'dropzone-api' created or verified.")

    # 4. Get Client UUID to ensure realm-management / service account if needed
    clients = make_request(f"{BASE_URL}/admin/realms/dropzone/clients?clientId=dropzone-api", token=token)
    if clients:
        client_uuid = clients[0]["id"]
        print(f"Client UUID: {client_uuid}")

    # 5. Create Users & Assign Roles
    users_data = [
        {"username": "john@example.com", "email": "john@example.com", "firstName": "John", "lastName": "Smith", "role": "USER"},
        {"username": "organizer@example.com", "email": "organizer@example.com", "firstName": "Organizer", "lastName": "User", "role": "ORGANIZER"},
        {"username": "support@example.com", "email": "support@example.com", "firstName": "Support", "lastName": "User", "role": "SUPPORT"},
        {"username": "admin@example.com", "email": "admin@example.com", "firstName": "Admin", "lastName": "User", "role": "ADMIN"},
    ]

    for u in users_data:
        users_url = f"{BASE_URL}/admin/realms/dropzone/users"
        user_body = {
            "username": u["username"],
            "email": u["email"],
            "firstName": u["firstName"],
            "lastName": u["lastName"],
            "enabled": True,
            "credentials": [{"type": "password", "value": "password", "temporary": False}]
        }
        make_request(users_url, method="POST", body=user_body, token=token)

        # Get User ID
        fetched_users = make_request(f"{BASE_URL}/admin/realms/dropzone/users?username={urllib.parse.quote(u['username'])}", token=token)
        if fetched_users:
            user_id = fetched_users[0]["id"]
            # Fetch Role Representation
            role_rep = make_request(f"{BASE_URL}/admin/realms/dropzone/roles/{u['role']}", token=token)
            if role_rep:
                # Assign role to user
                role_assign_url = f"{BASE_URL}/admin/realms/dropzone/users/{user_id}/role-mappings/realm"
                make_request(role_assign_url, method="POST", body=[role_rep], token=token)
                print(f"User '{u['username']}' assigned role '{u['role']}'.")

if __name__ == "__main__":
    setup_keycloak()
