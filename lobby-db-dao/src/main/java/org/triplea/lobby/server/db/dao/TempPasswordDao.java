package org.triplea.lobby.server.db.dao;

import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * DAO for CRUD operations on temp password table. A table that stores temporary passwords issued to
 * them with the 'forgot password' feature.
 */
public interface TempPasswordDao {
  @SqlQuery(
      "select temp_password"
          + " from user_temp_password t"
          + " join lobby_user lu on lu.id = t.lobby_user_id"
          + " where lu.username = :username"
          + "   and t.date_invalidated is null")
  Optional<String> fetchTempPassword(@Bind("username") String username);

  @SqlQuery("select id from lobby_user where username = :username")
  Optional<Integer> lookupUserIdByUsername(@Bind("username") String username);

  @SqlUpdate(
      "insert into user_temp_password"
          + " (lobby_user_id, temp_password)"
          + " values (:userId, :password)")
  void insertPassword(@Bind("userId") int userId, @Bind("password") String password);

  @SqlUpdate(
      "update user_temp_password"
          + " set date_invalidated = now()"
          + " where lobby_user_id = (select id from lobby_user where username = :username)"
          + "   and date_invalidated is null")
  void invalidateTempPasswords(@Bind("username") String username);

  default boolean insertTempPassword(final String username, final String password) {
    invalidateTempPasswords(username);
    final Optional<Integer> userId = lookupUserIdByUsername(username);
    if (userId.isEmpty()) {
      return false;
    }
    insertPassword(userId.get(), password);
    return true;
  }

  @SqlQuery("select email from lobby_user where username = :username")
  Optional<String> lookupUserEmail(@Bind("username") String username);
}