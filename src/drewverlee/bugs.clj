;; # Seven Troubleshooting Tips for Squashing Software Bugs

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html
 [:img {:src "https://docs.google.com/drawings/d/e/2PACX-1vQ6rsadfjdL3n7KjCPLGknAhV5x8jen8M0xvyVWtNGrAPat80_BFyvkl7xLjyReY383gXGOCATZ9G56/pub?w=960&amp;h=720"}])

;; How do you fix bugs in your software? Is there a process, and if so, can it
;; be taught, or is it a path that has to be walked? These questions came to me
;; on the heels of another that I was asked recently on a job application:

;; > Describe a bug you had a primary role in fixing. How did you troubleshoot and resolve the issue?

;; Well, I thought, do you want the long or short answer? The short you say? Fine then,
;; the bug was a mistranslation, and to troubleshoot, I tested, and typed until I
;; *triumphed!*

;; Too short? Maybe, but I fear the full story would be too long.

;; Can we settle on somewhere in between? Maybe find that middle ground between
;; a flattering short lie, and the confusing long truth.

;; Most importantly... has the interviewer already stopped reading this and
;; moved on? Maybe, but I have to hope they enjoy hearing about the journey.
;; And journeys aren't straight line, there winding rivers
;; that flows back into themselves. The landscape around us changes very little, and
;; in the end, it's our precipitations and not the world the bends.

;; So then, let me set you along the riverbank at a place which could be called
;; a beginning. From there, you will travel onward until the end, and your task will be to
;; keep your eyes open and see if you can catch the bug before it bites us.

;; Let me motivate the problem we are trying to solve, after all, if you don't
;; understand the pain, how can you hope to understand what there is to gain?
;; That sounds catchy, let's write it down as our first troubleshooting tip:


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn tip
  [message styles]
  [:p {:style
       (merge
        {:padding       "10px"
         :border-radius "10px"
         :font-size     "x-large"
         :text-align    "center"}
        styles)} message])

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn tip!
  [message styles]
  (clerk/html (tip message styles)))

^{:nextjournal.clerk/visibility {:code :hide}}
(tip! "No Pain, No Gain" {:background-color "#a12f2f"
                          :color            "white"})

;; Great, we hit our first tip, let's introduce the pain by understanding how we got there.

;; So then, imagine your with your friends on a trip, and you stop and have lunch,
;; the bill comes and the waiter didn't split it. Not wanting to spoil the
;; moment with the technicalities, you offer graciously to pay for everyone.
;; Tomorrow, at dinner, someone else covers the part. The trend of having
;; someone pay for the group continues.

;; However at the end of the trip, everyone becomes suddenly concerned they
;; didn't pay enough, but they are not sure who owes who what. Here is our painful
;; headache, we avoided the cost of settling up each night, only to delay it
;; until the end. As a result, we have a ledger of debts that need to be balanced,
;; they might look something like this?

;; * drew buys kirsten a 10 ice cream cone.
;;  * kirsten buys drew a 5 dollar soda.
;;  * drew buys katie a 5 dollar candy.

;; lets clean that up a bit:

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def loans
  [{:loaner "drew" :loanee "kirsten" :loan 10}
   {:loaner "kirsten" :loanee "drew" :loan 5}
   {:loaner "drew" :loanee "kaie" :loan 5}])

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/table {:nextjournal.clerk/width :prose} loans)


;; We need to turn this into a set of loans to be repaid. Oh, and wouldn't it be
;; a nice, because time is money, and sometimes there are transferring fees, to
;; guarantee it's the minimal number of loans needed? Avoiding cycles like:
;; drew paying kirsten 10, and then kirsten turns around and pays drew 5 of that back.

;; At this point, you may be tempted to ask chatGPT or search Google for similar
;; problems. However, there is no strong reason to because we haven't hit
;; anything we can't resolve ourselves.

;; Is that another tip? You bet:

^{:nextjournal.clerk/visibility {:code :hide}}
(tip! "Look inward before outward"
     {:background-color "blue" :color "white"})

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(require '[nextjournal.clerk :as clerk])

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def mermaid-viewer
  {:transform-fn clerk/mark-presented
   :render-fn    '(fn [value]
                    (when value
                      [nextjournal.clerk.render/with-d3-require {:package ["mermaid@8.14/dist/mermaid.js"]}
                       (fn [mermaid]
                         [:div {:ref (fn [el] (when el
                                                (.render mermaid (str (gensym)) value #(set! (.-innerHTML el) %))))}])]))})

;; Instead of searching, let's see if visualizing the problem helps, and a useful
;; visualization, like a useful description, will try to remove ambiguity. In
;; this case we can represent a loan by an arrow/directed-edge where the direction indicates
;; which way the money travels. Here is how we would translate the collection of loans above to a graph:

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(require '[arrowic.core :refer [create-graph graph-from-seqs insert-edge! insert-vertex! create-viewer]])

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def test-case
  [["Drew" "Kirsten" 10]
   ["Kirsten" "Drew" 5]
   ["Drew" "Katie" 5]])


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn edges->graph!
  [edges]
  (clerk/html
    (arrowic.core/as-svg
      (arrowic.core/graph-from-seqs edges))))

^{:nextjournal.clerk/visibility {:code :hide}}
(edges->graph! test-case)

;; And here is the graph after we consolidate the loans:

^{:nextjournal.clerk/visibility {:code :hide}}
(edges->graph!
  [["Drew" "Kirsten" 5]
   ["Drew" "Katie" 5]])

;; Finding a useful way to map the problem to physical space always helps. Lets write that down as another tip:

^{:nextjournal.clerk/visibility {:code :hide}}
(tip! "Paint the problem" {:background-color "purple" :color "white"})

;; This is just one example. Though, lets look at several more to make sure we
;; get the idea. We will want a fast way write and read the cases, so we want to be as concise as possible
;; TODO should that be a tip

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(require '[clojure.test :refer [is are deftest testing run-tests run-test]])

;; lets give our function a name to fit our current perception of the task:
^{:nextjournal.clerk/visibility {:result :hide}}
(declare loans->minimal-loans)

;; our tests should always have to justify themselves with a name, that way if they fail, we have some idea of what we thought we were testing
;; here is what I can think of so far:


[{:fn 'foo :input [0] :output 0}
 {:fn 'foo :input [1] :output 1}]


^{:nextjournal.clerk/visibility {:result :hide}}
(deftest test-loans->minimal-loans
  (testing "base cases"
    (is (= (loans->minimal-loans [])
           #{}))
    (is (= (loans->minimal-loans [{:loaner :a :loanee :b :loan 1}])
           #{{:loaner :a :loanee :b :loan 1}})))
  (testing "remove cycles"
    (is (= (loans->minimal-loans [{:loaner :a :loanee :b :loan 1} {:loaner :b :loanee :a :loan 1}])
           #{}))
    (is (= (loans->minimal-loans [{:loaner :a :loanee :b :loan 1} {:loaner :b :loanee :a :loan 2}])
           #{{:loaner :b :loanee :a :loan 1}})))
  (testing "unconnected nodes"
    (is (= (loans->minimal-loans [{:loaner :a :loanee :b :loan 1} {:loaner :c :loanee :d :loan 1}])
           #{{:loaner :c :loanee :b :loan 1} {:loaner :a :loanee :d :loan 1}}))))


;; ok, were almost ready to try and code a solution, but you might have noticed that we have two forms of expressing a loan, a hashmap:

^{:nextjournal.clerk/visibility {:result :hide}}
{:loaner "drew" "loanee" "kirsten" :loan 10}

;; and an edge expressed as a triplet

^{:nextjournal.clerk/visibility {:result :hide}}
["drew" "kirsten" 10]

;; both of these are correct in their own way, and it's worth having both
;; because the hashmap carries the business terminology, while the edge is more
;; generic and concise, this representation will make it easier to pattern match
;; our problem to others and re-use concepts from algorithms, graph theory,
;; mathematics etc...

;; I like this idea, lets add it to our troubleshooting tips as:

^{:nextjournal.clerk/visibility {:code :hide}}
(tip! "Map the translation"
     {:background-color "Orange" :color "white"})

;; lets go ahead and follow our own advice:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn loan->edge
  [{:keys [loaner loanee loan]}] [loaner loanee loan])

^{:nextjournal.clerk/visibility {:result :hide}}
(defn edges->nodes
  [edges]
  (->> edges
       (reduce
         (fn [n->v [start-node end-node edge-weight]]
           (-> n->v
               (update start-node (fnil + 0) edge-weight)
               (update end-node (fnil - 0) edge-weight)))
         {})
       (into #{})))

^{:nextjournal.clerk/visibility {:result :hide}}
(defn nodes->net-worths
  [nodes]
  (->> nodes
       (reduce
         (fn [net-worths [node-id node-label]]
           (conj net-worths {:user/id node-id :user/net-worth node-label}))
         #{})))

^{:nextjournal.clerk/visibility {:result :hide}}
(defn loans->net-worths
  [loans]
  (->> loans
       (map loan->edge)
       edges->nodes
       nodes->net-worths))

(loans->net-worths [{:loaner "drew" :loanee "kirsten" :loan 10}
                    {:loaner "drew" :loanee "katie" :loan 5}])



;; My first thought was that at the function, at each step would need to take two
;; net-worths, the largest and the smallest, and create a loan between them, and add back any reminder.

;; in order to keep our layers clear, lets translate that to taking two nodes,
;; with the largest and smallest labels, and creating a directed edge between
;; them from the largest to the smallest with largest value as their edge weight, AND add back the


;; Here is an implementation which does just that:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn nodes->minimal-edges
  "Given a set of nodes with integer labels return the graph with the minimal
  number of directed weighted integer edges such that a node would equal 0 after
  adding the incoming edge and subtracting out coming edges"
  [nodes]
  (loop [sorted-posative-nodes (->> nodes
                                    (filter #(pos? (second %)))
                                    (sort-by second <)
                                    vec)
         sorted-negative-nodes (->> nodes
                                    (filter #(neg? (second %)))
                                    (sort-by second >) vec)
         edges                 #{}]
    (if-not (seq sorted-posative-nodes)
      edges
      ;; smallest means "less then" e.g ;; (< -4 -1 ) -4 is smaller then -1.
      (let [[largest-posative-node-id largest-posative-node-integer-label]   (peek sorted-posative-nodes)
            [smallest-negative-node-id smallest-negative-node-integer-label] (peek sorted-negative-nodes)
            remainder-node-integer-label                                     (+ largest-posative-node-integer-label smallest-negative-node-integer-label)]
        (recur
         (cond-> (pop sorted-posative-nodes)
           (pos? remainder-node-integer-label)
           (->> (concat [[smallest-negative-node-id remainder-node-integer-label]])
                (sort-by second <)
                vec))
         (cond-> (pop sorted-negative-nodes)
           (neg? remainder-node-integer-label)
           (->> (concat [[smallest-negative-node-id remainder-node-integer-label]])
                (sort-by second >)
                vec))
         (conj edges [largest-posative-node-id
                      smallest-negative-node-id
                      largest-posative-node-integer-label]))))))

;; it looks like our test cases, while helping us refine our idea, will need
;; some translation from our existing cases which deal with edges to nodes, luckily we have a function for that `edges->nodes`
;; we can just apply that and continue with the addition of one extra test to capture that without two lists we could add a transaction.


^{:nextjournal.clerk/visibility {:result :hide}}
(deftest test-nodes->minimal-edges
  (testing "base cases"
    (is (= (nodes->minimal-edges [])                                   #{}))
    (is (= (nodes->minimal-edges #{[:b -1] [:a 1]})                    #{[:a :b 1]})))
  (testing "remove cycles"
    (is (= (nodes->minimal-edges #{[:a 0] [:b 0]})                     #{}))
    (is (= (nodes->minimal-edges #{[:a -1] [:b 1]})                    #{[:b :a 1]})))
  (testing "unconnected nodes"
    (is (= (nodes->minimal-edges #{[:b -1] [:c 1] [:d -1] [:a 1]})    #{[:c :b 1] [:a :d 1]})))
  (testing "uneven positive and negative nodes"
    (is (= (nodes->minimal-edges #{[:a 1] [:b 2] [:c -3]})    #{[:a :c 1] [:b :c 2]}))))


(run-test test-nodes->minimal-edges)

;; all the tests passed. Great that means all we have to do is translate our edges back to loans, here is the function to do that:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn edge->loan
  [[start-node end-node edge-weight]]
  (if (pos? edge-weight)
    {:loaner start-node :loanee end-node :loan edge-weight}
    {:loaner end-node :loanee start-node :loan (abs edge-weight)}))

;; Now wrap that together into the function we were aiming for at the start,
;; notice that our mapping between the business domain and the graph layer is
;; very clear:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn loans->minimal-loans
  [loans]
  (->> loans
       (map loan->edge)
       edges->nodes
       nodes->minimal-edges
       (map edge->loan)
       set))

;; lets call our tests

(run-test test-loans->minimal-loans)

;; success!

;; ...
;; Or is it? Didn't I warn you this didn't have a happy ending? I assure you, despite our tests, and careful planning
;; we failed. Do you see the issue? The problem is... I don't have the words to
;; describe it, so let give you a glimpse of it. To do that, look at a set of net-worths/node-labels/integers:

^{:nextjournal.clerk/visibility {:result :hide}}
(def integers [-9 -8 -4 -2 -1 3 5 6 10])

;; Does anything jump out at you? I don't know why it would, but let me show you
;; in a picture why this set of integers is interesting. To do that, first lets
;; turn it into a list of nodes. First we make a helper to convert integers into
;; nodes by assigning the node-label a character as a node-id:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn integers->nodes
  [integers]
  (into #{} (zipmap  (map char (range 97 123)) integers)))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn nodes->label->node-ids
  [nodes]
  (reduce
    (fn [label->node-ids [node-id node-label]]
      (update label->node-ids node-label conj node-id))
    {}
    nodes))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(def label->node-ids (-> integers integers->nodes nodes->label->node-ids))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn label->node-ids+labels->nodes
  [label->node-ids labels]
  (loop [label->node-ids label->node-ids
         labels labels
         nodes []]
    (if-not (seq labels)
      nodes
      (let [label (first labels)
            node-id (-> label label->node-ids first)
            node [node-id label]]
        (recur
          (update label->node-ids label rest)
          (rest labels)
          (conj nodes node))))))

;; Then another helper function to bring that together with the graphing function we have been using behind the scenes:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn integers->min-edge-graph!
  [integers]
  (->> integers
       integers->nodes
       nodes->minimal-edges ;; <-- the function we put our faith in.
       edges->graph!))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn nodes->edges
  [nodes]
  (loop [nodes (vec nodes)
         edges []]
    (let [[start-node-id start-node-label] (peek nodes)
          [end-node-id end-node-label] (peek (pop nodes))
          new-end-node-label (+ start-node-label end-node-label)
          new-edges (conj edges [start-node-id end-node-id start-node-label])]
      (if (zero? new-end-node-label)
        new-edges
        (recur
         (-> nodes pop pop (conj [end-node-id new-end-node-label]))
         new-edges)))))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(require '[clojure.math.combinatorics :refer [permutations combinations]])

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn integers->zero-sum-subsets
  [integers]
  (->> integers
       (reduce
        (fn [{:keys [zero-sum-subsets zero-sum-subset]} integer]
          (let [zero-sum-subset (conj zero-sum-subset integer)]
            (if (zero? (apply + zero-sum-subset))
              {:zero-sum-subsets (conj zero-sum-subsets zero-sum-subset) :zero-sum-subset []}
              {:zero-sum-subsets zero-sum-subsets :zero-sum-subset zero-sum-subset})))
        {})
       :zero-sum-subsets
       set))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn integers->max-zero-sum-subsets
  [integers]
  (->> integers
       permutations
       (pmap integers->zero-sum-subsets)
       (sort-by count)
       last))

^{:nextjournal.clerk/visibility {:result :hide :code :hide}}
(defn integers->min-edge-graph-v2!
  [integers]
  (->> integers
       integers->max-zero-sum-subsets
       (reduce #(conj %1 (label->node-ids+labels->nodes label->node-ids %2)) #{})
       (mapcat nodes->edges)
       edges->graph!))

;; and we can use to go from a set of integers to a graph:

(integers->min-edge-graph! integers)

;; Great! Except here is another function, that takes the same set of integers, and finds what is clearly less transactions/loans/edges:

(integers->min-edge-graph-v2! integers)

;; This is the Bug! Our tips so far weren't enough to protect us from it, but
;; thats ok were only half way through them. And before we take the next step and tackle
;; this pest, I want to take a moment and discuss why i think it's important to
;; take a step back and think about how we framed this issue in the first place.


;; In the software community a 'bug' commonly refers to any issue with the
;; software. To a entomogists, a bug seems to be anything with a piercing mouth
;; that sucks juices from plants or animals. Around maybe a 1,000 years ago
;; 'bug' roughly meant bugbear. And now , to most people refers to those very
;; little things that fly or crawl around.

;; The common theme here, except for the entomologist, is that bugs at best, are
;; useless, and likely are irritating creatures that most would like to remove
;; from the situation.

;; I feel this outlook, when applied to troubleshooting a software problem, of
;; assuming the problem is something to be _removed_ is often misguided.

;; Instead, what i need to do to realize my desires is understand what those
;; really are in the first place. Put another way, most 'issues' I run across
;; feel, in retrospect, like I was upset that sunflowers didn't taste good
;; despite my constant care, and attention to watering them.

;; Put another way, I didn't need to remove something from my sunflower, it's simply that what I really
;; wanted to grow was tomatoes. I was growing the wrong, thing, so it was a weed.

;; I want make a suggestion to you, my reader, of not saying "we have a bug", but asking
;; Is this a bug or a weed? This is my next trouble shooting tip:

^{:nextjournal.clerk/visibility {:code :hide}}
(tip! "Weed Or Bug" {:background-color "green" :color "white"})


;; While this distintion seems fuzzy in that both bugs and weeds are both
;; undesirable things, however a weeds defining characteristic isn't that there
;; is something wrong with it, but that it's just not the plant you wanted.

;; So, the issue isn't the weed you have, it's the plant you don't. A weed
;; indicates something vital is lacking, and needs to be added. As where a bug
;; suggests something needs to be removed.

;; What matters is that the question starts to divide the problem. And breaking
;; the problem apart is the heart of effective troubleshooting.


^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(import '(javax.imageio ImageIO))

^{:nextjournal.clerk/visibility {:code :hide}}
(ImageIO/read (.toURL (.toURI (clojure.java.io/file "resources/bug-yin-yang.png"))))

;; So, do we have a bug or a weed? Is there something our program is doing wrong?
;; Or is lacking something?

;; Here is a hint, what didn't we actually test? Here are our tests for reference

^{:nextjournal.clerk/visibility {:result :hide}}
["base cases"
 "remove cycles"
 "unconnected nodes"
 "uneven positive and negative nodes"]

;; None of those claim to minimize transactions other then remove cycles, but are cycles
;; the only way you end up with extra transactions? Given the last graph we saw, clearly not.
;; Does removing a cycle help? kind of. Is it implemented correctly? Yes. Is it enough? No.

;; The question is how does our algorithm grow such at each step we ensure the
;; minimal number of transactions? First off, we need to strip away the
;; ambiguity.

;; A transaction/loan/edge is produced anytime we add two net-worths/integers/node-labels and
;; get a non-zero result.

;; So to avoid transactions, we want results that equal zero.

;; So given the choice, our algorithm should always pick 2 numbers that equal
;; zero. And given there is no option to do that, should it always remove one
;; node/net-worth/user from the set?

;; I think so, because if you don't the best you can do is eliminate two on your
;; next turn, which means it would have been just as good to eliminate 1 this turn and 1 the next.

;; So then, given no choice to remove-2 or remove-0, and we have to remove-1,
;; which one to remove? Put another way, which two numbers to pick! Well that
;; choice is recursive, you pick the two, that if not on this turn allow you to remove-2, then they will on the next,
;; and the next, etc....

;; what this do, if done correctly, is break our orginal set, really a multiset
;; (duplicates allowed), into as many subsets as possible where each subset, really a partition because, it sums to 0, and themselves contain no partition which sums to 0.

;; The 'easiest' way to find every possible subset that sums to zero, is to
;; take, every permutation of the orginal set, reduce over it, and collect
;; subsets that sum to zero by adding each integer encountered either to a
;; current-subset or, if that current-subset + new-integer equals zero, to the
;; collection of subsets that equal zero:

;;  * (1 -1 -2 2) 2 subsets because 1 + -1 = 2 and -2 + 2 = 0
;;  * (1 -2 -1 2) 1 subset because 1 + -2 = -1 and then -1 + -1 = -2 and finally -2 + 2 = 0

;; However the run time complexity of finding all permutations that is n! or (n)(n-1)..(n-n).

;; So is there a better way? Or maybe caching can help?
;; I wasn't able to see any better solutions, at first I thought if i could find
;; a way to select two numbers, such that my next selection would sum to 0, that would help,
;; but i quickly realized thats just kicking the can, as a 0 sum might not be possible in the next
;; solution either. This principle seems to apply to picking three numbers that sum to zero as well.

;; a bit disheartened, because I knew a solution with permutations wouldn't scale very well,
;; I quickly coded it to at least soldiify the idea. Just as described, we need something
;; to turn our set of integers into subsets that sum to zero:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn integers->zero-sum-subsets
  [integers]
  (->> integers
       (reduce
        (fn [{:keys [zero-sum-subsets zero-sum-subset]} integer]
          (let [zero-sum-subset (conj zero-sum-subset integer)]
            (if (zero? (apply + zero-sum-subset))
              {:zero-sum-subsets (conj zero-sum-subsets zero-sum-subset) :zero-sum-subset []}
              {:zero-sum-subsets zero-sum-subsets :zero-sum-subset zero-sum-subset})))
        {})
       :zero-sum-subsets
       set))

;; and use that to produce our zero-sum-subsets for one permutation

(map integers->zero-sum-subsets [[1 -1 -2 2] [1 2 -2 -1]])

;; and finally we just have to pick the permutation which produces the most zero-sum-subsets...

^{:nextjournal.clerk/visibility {:result :hide}}
(defn integers->max-zero-sum-subsets
  "Given a list of integers that sum to zero, partition them into the most
  subsets possible which also sum to 0, but which themselves contain no subsets
  that sum to zero."
  [integers]
  (->> integers
       permutations
       (pmap integers->zero-sum-subsets)
       (sort-by count)
       last))

;; thats how we got our example graph that had fewer edges from before:

^{:nextjournal.clerk/visibility {:result :hide}}
(defn integers->min-edge-graph-v2!
  [integers]
  (->> integers
       integers->max-zero-sum-subsets ;;<-- all the work is done here the rest is setup and tear down
       (mapcat integers->zero-sum-subsets)
       (reduce #(conj %1 (label->node-ids+labels->nodes label->node-ids %2)) #{})
       (mapcat nodes->edges)
       edges->graph!))

;; Is that it then, are we doomed to a factorial time solution if we want to
;; guarantee the maz-zero-sum? I'm not sure honestly. I'm also not sure anything can be cached, aka, we can
;; use Dynamic programming, to make this better. After all, what would we cache? The mapping between a sum of a set and the set?
;; That would mean that any sets we cached would themselves contain sets/integers that were guartneed to be 'the right ones'.
;; The problem doesn't seem like it reduces. But maybe I'm wrong. Time to look afield, and this brings us to another tip...

(tip! "Ask Good Questions"
      {:background-color "blue" :color "white"})

;; an answer is only as good as it's question. And so now that were armed with a good question, which
;; is the doc string of our function:

;; > TODO

;; we can start to listen to the world around us and have a chance of cutting through the noise. And so here
;; at the end I promised i would explain why its better to avoid searching
;; for the answer to early before your very sure what your looking for.

;; Maybe our AI overloards can help? Here is what i asked ChatGPT 4o:

;; > Given a list of integers that sum to zero, return the maximum partitions of that
;; > set, which sum to zero but contain no parititions themselves, which sum to zero.

;; here is the code it produced:

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/md "```python
 def max_zero_sum_partitions(nums):
    n = len(nums)
    cumulative_sum = 0
    sum_to_index = {0: -1}
    partitions = []
    last_end = -1

    for i in range(n):
        cumulative_sum += nums[i]

        if cumulative_sum in sum_to_index:
            start = sum_to_index[cumulative_sum] + 1
            end = i

            if start > last_end:
                partitions.append((start, end))
                last_end = end

        sum_to_index[cumulative_sum] = i
    return partitions
# Example usage
nums = [1, -1, 2, -2, 3, -3, 4, -4, 5, -5]
partitions = max_zero_sum_partitions(nums)
print(\"Partitions (start, end):\", partitions)
print(\"Number of partitions:\", len(partitions))
```")


;; Feel free to think that one through, but it doesn't solve the problem. I
;; found if asked a more general question about minimizing transactions it
;; would through out equally vague promises followed by incorrect specifics.

;; Lets move past AI and try good google search and stackover flow.

;; The [first S0](
;; https://stackoverflow.com/questions/877728/what-algorithm-to-use-to-determine-minimum-number-of-actions-required-to-get-the) [second](https://softwareengineering.stackexchange.com/questions/337125/finding-the-minimum-transaction)
;; and [third](https://softwareengineering.stackexchange.com/questions/337125/finding-the-minimum-transaction) link also, as afar as i can tell, fail to suggest an algorithm which guarantees minimal transactions

;; I then felt like i should look into what the competition was doing and found that SplitWise was using the
;; the [minimum cost flow algorithm](https://en.wikipedia.org/wiki/Minimum-cost_flow_problem), which i'm relative sure,
;; doesn't guarantee the minimal amount of transactions either. This [post](https://medium.com/@subhrajeetpandey2001/splitwise-a-small-approach-of-greedy-algorithm-4039a1e919a6#:~:text=The%20Debt%20Simplification%20Algorithm%20used,as%20few%20edges%20as%20possible.) has this to say about the minimal cost flow algorithm:

;; > Here’s a simplified version of the algorithm:

;; > Calculate the net balance for each member in the group.
;; > While there are outstanding balances:
;; > Identify the member owed the most (vmax) and the member owing the most (vmin).
;; > Transfer the minimum of |vmax| and |vmin| from vmax to vmin.
;; > Update the net balances for both members. If the balance becomes 0, remove them from the set of vertices.
;; > 3. Repeat steps 2 until all balances are settled.

;; > This algorithm sacrifices the rule that “No one owes a person that they didn’t
;; >  owe before” but efficiently settles balances. While its time complexity is
;; >  O(V²) and space complexity is O(V), it may require more transactions than
;; >  necessary.

;; Two things to note there, the most important is that the author agrees with my
;; intution that it may require more transactions then necessary and if your read
;; the steps, it's basically what we tried on our second step after we realized we
;; needed two lists, so we KNOW it doesn't work.

;; And finally here is a
;; [post](https://medium.com/@mithunmk93/algorithm-behind-splitwises-debt-simplification-feature-8ac485e97688)
;; agreeing that the minimal transaction problem seems to be NP-complete.

;; > This indicates that the debt simplification problem is at least as hard as the Sum of Subsets Problem and hence it is NP-Complete

;; However the author is making, i believe, a gross understatment. the subset
;; problem finds out if there exists a subset of numubers that equal a given
;; number, discovering the maxium number of sets that the orginal set can break
;; into that equal that number might be a degree or two more work.

;; Does all this mean my idea is doomed? Not at all, it just means i have to be
;; careful in my planning. If i want to guarantee the minimal, which i do, then
;; ill, for the moment have to keep the input size small.

;; I don't have to give up on my idea just because i can't scale it to work with everything. This brings us to my final
;; tip:

(tip! "Bend don't Break" {:background-color "black" :color "white"})

;; The goal was inspired by a trip my friends went on, and how afterwards we had to figure out who owes who.
;;
