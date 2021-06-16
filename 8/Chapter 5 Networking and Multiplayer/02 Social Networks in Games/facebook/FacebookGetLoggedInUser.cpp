//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookGetLoggedInUser.h"

#include <QDomDocument>
#include <QVector>

//------------------------------------------------------------------------------
/*static*/ const QString FacebookGetLoggedInUserRequest::requestName("facebook.users.getLoggedInUser");

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetLoggedInUserRequest::operator()(const Input& /*input*/,
  QVector<QString>& /*parameters*/)
{
  // No input parameters.
  return FacebookServiceStates::Ok;
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetLoggedInUserResponse::operator()(
  const QDomDocument& document, Output& output)
{
  QDomNodeList uid = document.elementsByTagName("users_getLoggedInUser_response");
  output = uid.at(0).toElement().text();

  return FacebookServiceStates::Ok;
}
