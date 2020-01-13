#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iomanip>
#include <iostream>
#include <map>
#include <queue>
#include <set>
#include <sstream>
#include <string>
#include <vector>
#include <list>
#include <cassert>
#include <queue>

using namespace std;
template <class _T> inline _T sqr(const _T& x) { return x * x; }
template <class _T> inline _T ABS(const _T& x) { return (x<0)?-x:x;}
template <class _T> inline string tostr(const _T& a) { ostringstream os(""); os << a; return os.str(); }
template <class _T> inline istream& operator << (istream& is, const _T& a) { is.putback(a); return is; }
template <class _T> inline _T gcd(const _T &a, const _T &b) {
    _T t;

    while (!(b == 0)) {
        t = a % b;
        a = b;
        b = t;
    }

    return a;
}

typedef long double ld;


const ld PI = 3.1415926535897932384626433832795;
const ld EPS = 1e-11;


typedef unsigned uns;
typedef signed long long i64;
typedef unsigned long long u64;
typedef set < int > SI;
typedef vector < ld > VD;
typedef vector < int > VI;
typedef vector < bool > VB;
typedef vector < string > VS;
typedef pair < int, int > PII;
typedef map < string, int > MSI;
typedef map < string, void * > MSV;






static bool const _debug_ = false;






inline uns getUnsigned() {
    uns curr;
    scanf("%u", &curr);
    return curr;
}

inline void getUnsigned(uns &one, uns &two) {
    scanf("%u%u", &one, &two);
}

inline void getUnsigned(uns &one, uns &two, uns &three) {
    scanf("%u%u%u", &one, &two, &three);
}

inline int getInt() {
    int curr;
    scanf("%d", &curr);
    return curr;
}

inline void getInt(int &one, int &two) {
    scanf("%d%d", &one, &two);
}

inline void getInt(int &one, int &two, int &three) {
    scanf("%d%d%d", &one, &two, &three);
}

inline double getDouble() {
    double curr;
    scanf("%lf", &curr);
    return curr;
}

inline void getDouble(double &one, double &two) {
    scanf("%lf%lf", &one, &two);
}

inline void getDouble(double &one, double &two, double &three) {
    scanf("%lf%lf%lf", &one, &two, &three);
}

inline void FLUSH() {
    string dummy;
    getline(cin, dummy);
}

inline string getString() {
    string curr;
    cin >> curr;
    return curr;
}

inline string getLine() {
    string curr;
    getline(cin, curr);
    return curr;
}

inline void split(string const &in, VS &out, char delim = ' ') {
    size_t start = 0; size_t len = 0;
    size_t end = in.size() -1;
    size_t foundAt = in.find_first_of(delim, start);
    while (foundAt != string::npos) {
        len = (foundAt - start);
        out.push_back(in.substr(start, len));
        start = foundAt+1;
        foundAt = in.find_first_of(delim, start);
    }
    if (foundAt != end) {
        out.push_back(in.substr(start));
    }
}





inline void foreachTest(uns testNum) {

    int row1;
    cin >> row1;

    int r, c, card;
    set<int> crow1;
    for (r = 1; r<=4; ++r) {
        for (c = 1; c<=4; ++c) {
            cin >> card;

            if (r == row1) {
                crow1.insert(card);
            }
        }

    }
    int row2;
    cin >> row2;

    set<int> crow2;
    for (r = 1; r<=4; ++r) {
        for (c = 1; c<=4; ++c) {
            cin >> card;

            if (r == row2) {
                crow2.insert(card);
            }
        }

    }


    set<int> inters;
    set_intersection(crow1.begin(), crow1.end(),
            crow2.begin(), crow2.end(),
            std::inserter(inters,inters.begin()));
    cout << "Case #" << testNum << ": ";
    size_t st = inters.size();

    if (st == 1U) {
        cout << *(inters.begin());
    } else if (st == 0U) {
        cout << "Volunteer cheated!";
    } else {
        cout << "Bad magician!";
    }


    cout << endl;
}

int main() {



    cout << setiosflags(ios::fixed) << setprecision(10);

    uns T = getUnsigned();
    for (uns tt=1U; tt<=T; ++tt) {

        foreachTest(tt);
    }
    return 0;
}
